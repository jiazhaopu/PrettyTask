package com.jzp.task.revolver;

import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.storage.DBDataSource;
import com.jzp.task.revolver.storage.TaskInfo;

import java.util.List;

public class TaskClient extends TaskAbstractClient {

  public TaskClient(List<DBDataSource> dbDataSources, Config config) {
    super(dbDataSources, config);
  }

  @Override
  public void init() throws Exception {
    super.init();
  }

  @Override
  public void close() {
    super.close();
  }

  public TaskInfo register(TaskInfo taskInfo) throws Exception {
    return super.register(taskInfo);
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
