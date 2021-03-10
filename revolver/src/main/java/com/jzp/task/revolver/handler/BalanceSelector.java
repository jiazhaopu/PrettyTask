package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.TaskInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BalanceSelector implements IPoolSelector {

  private final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

  private final AtomicInteger atomicInteger = new AtomicInteger();

  @Override
  public int select(TaskInfo taskInfo) {
    Integer index = map.get(taskInfo.getHandler());
    if (index == null) {
      index = next();
      map.put(taskInfo.getHandler(), index);
    }
    return index;
  }

  private int next() {
    int value = atomicInteger.incrementAndGet();
    if (value >= Context.getConfig().getExecutePoolsNum()) {
      atomicInteger.set(0);
      return 0;
    }
    return value;
  }
}
