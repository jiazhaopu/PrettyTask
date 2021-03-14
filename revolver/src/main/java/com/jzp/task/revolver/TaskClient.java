package com.jzp.task.revolver;

import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.CronTask;
import com.jzp.task.revolver.storage.DBDataSource;
import com.jzp.task.revolver.storage.FixedTask;
import com.jzp.task.revolver.storage.RetryTask;
import com.jzp.task.revolver.storage.TaskInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;

public class TaskClient extends TaskAbstractClient {

  public TaskClient(List<DBDataSource> dbDataSources, Config config) throws Exception {
    super(dbDataSources, config);
    init();
  }

  @Override
  protected void init() throws Exception {
    super.init();
    Context.setTaskClient(this);
  }

  @Override
  public void close() {
    super.close();
  }


  public TaskInfo registerCron(@Validated CronTask cronTask) throws Exception {
    TaskInfo info = new TaskInfo();
    info.setCron(cronTask.getCron());
    info.setName(cronTask.getName());
    info.setMaxExecuteTimes(cronTask.getMaxExecuteTimes());
    info.setScheduleType(ScheduleType.CRON.getCode());
    info.setHandler(cronTask.getHandler().getName());
    info.setContent(cronTask.getContent());
    return super.register(info);
  }

  public TaskInfo registerFixed(@Validated FixedTask fixedTask)
      throws Exception {
    TaskInfo info = new TaskInfo();
    info.setNextTime(fixedTask.getExecuteTime());
    info.setName(fixedTask.getName());
    info.setScheduleType(ScheduleType.FIXED_TIME.getCode());
    info.setMaxExecuteTimes(1);
    info.setHandler(fixedTask.getHandler().getName());
    info.setContent(fixedTask.getContent());
    return super.register(info);
  }


  public TaskInfo registerRetry(@Validated RetryTask retryTask)
      throws Exception {
    TaskInfo info = new TaskInfo();
    info.setName(retryTask.getName());
    info.setCron(retryTask.getCron());
    info.setScheduleType(ScheduleType.RETRY.getCode());
    info.setMaxExecuteTimes(retryTask.getMaxTimes());
    info.setHandler(retryTask.getHandler().getName());
    info.setContent(retryTask.getContent());
    return super.register(info);
  }

  public boolean suspend(Integer id) throws Exception {
    return super.suspendById(id);
  }


  public boolean start(Integer id) throws Exception {
    return super.start(id);
  }


  @Override
  public List<DBDataSource> getDbDataSources() {
    return super.getDbDataSources();
  }

  @Override
  public void setDbDataSources(List<DBDataSource> dbDataSources) {
    super.setDbDataSources(dbDataSources);
  }
}
