package com.jzp.task.revolver;

import com.jzp.task.revolver.constants.State;
import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.model.TaskInfo;
import com.jzp.task.revolver.register.RegisterCenter;
import com.jzp.task.revolver.register.ZookeeperClient;
import com.jzp.task.revolver.utils.CronUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class TaskAbstractClient {
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
    Context.getState().set(State.CREATE);
  }


  /**
   * 初始化内部mysql 连接池，线程池等
   */
  public void init() throws Exception {
    if (State.RUNNING.equals(Context.getState().get())) {
      log.info("TransactionTaskClient have inited, return");
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
    Context.getState().compareAndSet(State.CREATE, State.RUNNING);
    log.info("end init success");
  }


  public void close() {
    log.info("start close TransactionTaskClient");
    if (Context.getState().compareAndSet(State.RUNNING, State.CLOSED)) {
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
  protected TaskInfo register(TaskInfo taskInfo) throws Exception {
    if (!Context.getState().get().equals(State.RUNNING)) {
      log.error("TransactionTaskClient not Running , please call init function");
      throw new Exception("TransactionTaskClient not Running , please call init function");
    }
    if (!CronUtil.checkCron(taskInfo)) {
      throw new Exception("cron is not valid");
    }
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



}
