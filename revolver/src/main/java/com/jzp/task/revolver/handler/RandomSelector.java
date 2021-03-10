package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.TaskInfo;
import org.apache.commons.lang3.RandomUtils;

public class RandomSelector implements IPoolSelector {
  @Override
  public int select(TaskInfo taskInfo) {
    return RandomUtils.nextInt(0, Context.getConfig().getExecutePoolsNum());
  }
}
