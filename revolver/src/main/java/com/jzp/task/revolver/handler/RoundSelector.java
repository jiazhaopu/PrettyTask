package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.TaskInfo;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundSelector implements IPoolSelector {

  private final AtomicInteger atomicInteger = new AtomicInteger();

  @Override
  public int select(TaskInfo taskInfo) {
    int value = atomicInteger.incrementAndGet();
    if (value >= Context.getConfig().getExecutePoolsNum()) {
      atomicInteger.set(0);
      return 0;
    }
    return value;
  }
}
