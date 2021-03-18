package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.executor.ExecutePool;
import com.jzp.task.revolver.storage.TaskInfo;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 选择当前负载最低的线程池，优点效率高，缺点执行线程池不确定，多线程并发不好控制
 */
public class LowLoadSelector implements IPoolSelector {

  @Override
  public int select(TaskInfo taskInfo) {
    ThreadPoolExecutor[] executors = ExecutePool.getInstance().getExecutePools();
    ThreadPoolExecutor executor;
    Integer count = null;
    int index = 0;
    for (int i = 0; i < executors.length; i++) {
      executor = executors[i];
      if (executor == null || executor.getActiveCount() == 0) {
        return i;
      }
      if (count == null || executor.getActiveCount() < count) {
        count = executor.getActiveCount();
        index = i;
      }

    }
    return index;
  }
}
