package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.TaskInfo;
import org.apache.commons.lang3.StringUtils;

public class HashSelector implements IPoolSelector {

  @Override
  public int select(TaskInfo taskInfo) {
    String key = StringUtils.isEmpty(taskInfo.getName()) ? taskInfo.getHandler() : taskInfo.getName();
    return key.hashCode() % Context.getConfig().getExecutePoolsNum();
  }
}
