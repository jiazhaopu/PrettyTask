package com.jzp.task.revolver;

import com.jzp.task.revolver.constants.ServerState;
import com.jzp.task.revolver.constants.TaskStatus;
import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.log.ILogger;
import com.jzp.task.revolver.register.RegisterCenter;
import com.jzp.task.revolver.register.ZookeeperClient;
import com.jzp.task.revolver.storage.DBDataSource;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.storage.TaskStorage;
import com.jzp.task.revolver.utils.CronUtil;
import com.jzp.task.revolver.utils.IPUtils;
import com.jzp.task.revolver.utils.TaskUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class TaskAbstractClient implements ILogger {
  protected static final Logger log = LoggerFactory.getLogger(TaskAbstractClient.class);
  private List<DBDataSource> dbDataSources;
  private TaskStorage taskStorage;
  private TaskProcessor taskProcessor;

  /**
   * @param config
   * @param dbDataSources 数据源，用户名，密码，url Url需要和业务dataSource中配置的url一模一样
   */
  protected TaskAbstractClient(List<DBDataSource> dbDataSources, Config config) {
    this.dbDataSources = dbDataSources;
    taskStorage = new TaskStorage(dbDataSources);
    taskProcessor = new TaskProcessor();
    if (config != null) {
      Context.setConfig(config);
    }
    Context.getState().set(ServerState.CREATE);
  }


  /**
   * 初始化内部mysql 连接池，线程池等
   */
  protected void init() throws Exception {
    if (ServerState.RUNNING.equals(Context.getState().get())) {
      log.info("Revolver have inited, return");
      return;
    }
    ZookeeperClient client = new ZookeeperClient(Context.getConfig());
    RegisterCenter registerCenter = new RegisterCenter(client);
    Context.setZookeeperClient(client);
    Context.setRegisterCenter(registerCenter);
    taskStorage.init();
    taskProcessor.init();
    Context.setTaskProcessor(taskProcessor);
    Context.setTaskStorage(taskStorage);
    registerCenter.registerNode();
    registerCenter.beatsAndWatcher();
    taskProcessor.reloadTask();
    Context.getState().compareAndSet(ServerState.CREATE, ServerState.RUNNING);
    log.info("end init success");
  }


  public void close() {
    log.info("start close Revolver TaskClient");
    if (Context.getState().compareAndSet(ServerState.RUNNING, ServerState.CLOSED)) {
      taskStorage.close();
      taskProcessor.close();
    } else {
      log.info("state not right {} ", Context.getState());
    }
  }


  /**
   * 如果我们拿不到连接，需要暴露出来，让业务方set Connection
   *
   * @return
   * @throws Exception
   */
  public TaskInfo register(TaskInfo taskInfo) throws Exception {
    if (!Context.getState().get().equals(ServerState.RUNNING)) {
      log.error("Revolver not Running , please call init function");
      throw new Exception("Revolver TaskClient not Running , please call init function");
    }
    taskInfo.setHost(IPUtils.getHostAddress());
    TaskUtil.checkRegisterAndStart(taskInfo);
    try {
      taskInfo = taskStorage.register(taskInfo);
      taskProcessor.put(taskInfo);
    } catch (Exception ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
      throw ex;
    }
    return taskInfo;
  }

  public List<DBDataSource> getDbDataSources() {
    return dbDataSources;
  }

  public void setDbDataSources(List<DBDataSource> dbDataSources) {
    this.dbDataSources = dbDataSources;
  }


  public boolean suspendById(Integer id) throws Exception {
    TaskInfo taskInfo = taskStorage.selectForUpdate(id);
    if (taskInfo != null) {
      if (TaskStatus.canSuspend(taskInfo.getStatus())) {
        taskStorage.updateStatus(id, taskInfo.getStatus(), TaskStatus.SUSPEND.getCode());
        taskProcessor.remove(taskInfo);
        return true;
      }
    }
    return false;
  }

  public boolean start(Integer id) throws Exception {
    TaskInfo taskInfo = taskStorage.selectForUpdate(id);
    if (taskInfo != null) {
      if (TaskStatus.canStart(taskInfo.getStatus())) {
        TaskUtil.checkRegisterAndStart(taskInfo);
        int row = taskStorage.restart(id, taskInfo.getStatus(), CronUtil.nextExecuteTime(taskInfo));
        if (row > 0) {
          taskProcessor.put(taskInfo);
          return true;
        }
      }
    }
    return false;
  }


}
