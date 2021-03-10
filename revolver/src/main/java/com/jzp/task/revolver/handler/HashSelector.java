package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.TaskInfo;

public class HashSelector implements IPoolSelector {

  @Override
  public int select(TaskInfo taskInfo) {
    return taskInfo.getHandler().hashCode() % Context.getConfig().getExecutePoolsNum();
  }
}
