package com.jzp.task.aync;

import java.util.List;

public class TaskClient extends TaskAbstractClient {

  public TaskClient(List<DBDataSource> dbDataSources, Config config) {
    super(dbDataSources, config);
    init();
  }

  @Override
  public void init() {
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
