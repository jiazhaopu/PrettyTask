package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.TaskInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BalanceSelector implements IPoolSelector {

  private final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

  private final AtomicInteger atomicInteger = new AtomicInteger();

  @Override
  public int select(TaskInfo taskInfo) {
    String key = StringUtils.isEmpty(taskInfo.getName()) ? taskInfo.getHandler() : taskInfo.getName();
    Integer index = map.get(key);
    if (index == null) {
      index = next();
      map.put(key, index);
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
