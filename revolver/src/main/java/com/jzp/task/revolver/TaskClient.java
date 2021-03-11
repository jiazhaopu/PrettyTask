package com.jzp.task.revolver;

import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.DBDataSource;
import com.jzp.task.revolver.storage.TaskInfo;

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

  public TaskInfo register(TaskInfo taskInfo) throws Exception {
    return super.register(taskInfo);
  }

  public boolean suspend(Integer id) {
    return super.suspendById(id);
  }


  public boolean start(Integer id) {
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
