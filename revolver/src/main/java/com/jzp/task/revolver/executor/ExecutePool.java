package com.jzp.task.revolver.executor;

import com.jzp.task.revolver.constants.PoolSelectorEnum;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.handler.IPoolSelector;
import com.jzp.task.revolver.handler.ITaskCallBack;
import com.jzp.task.revolver.handler.ITaskHandler;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.utils.ApplicationContextHelper;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutePool {

  private static final ExecutePool pool = new ExecutePool();

  private IPoolSelector selector;

  private final ThreadPoolExecutor[] executePools;

  public static ExecutePool getInstance() {
    return pool;
  }

  private ExecutePool() {
    executePools = new ThreadPoolExecutor[Context.getConfig().getExecutePoolsNum()];
  }

  private ThreadPoolExecutor getExecutor(TaskInfo taskInfo) {
    int index = select(taskInfo);
    ThreadPoolExecutor executor = executePools[index];
    if (executor == null) {
      return initExecutor(index);
    }
    return executor;
  }

  private synchronized ThreadPoolExecutor initExecutor(int hash) {
    ThreadPoolExecutor executors = executePools[hash];
    if (executors == null) {
      executePools[hash] = makeExecutePool();
      return executePools[hash];
    }
    return executors;
  }

  public void submit(final TaskInfo taskInfo, ITaskCallBack callable) {
    ThreadPoolExecutor executor = getExecutor(taskInfo);
//      System.out.println("submit pool_queueSize="+executor.getQueue().size() + ", id="+taskInfo.getId());
    executor.execute(() -> {
      try {
        ITaskHandler handler = (ITaskHandler) ApplicationContextHelper.getBean(taskInfo.getHandler());
        if (handler == null) {
          return;
        }
        long t = System.currentTimeMillis();
//        System.out.println("start execute id=" + taskInfo.getId() + " executorPoolSize=" + executor.getActiveCount()
//            + ", pool_queueSize=" + executor.getQueue().size());
        System.out.println(
            "start execute taskInfo=" + taskInfo.toString() + " nextTime=" + new Date(taskInfo.getNextTime())
                + ",nowDate=" + new Date());
        boolean res = handler.execute(taskInfo.getContent());
        callable.call(taskInfo, res);
//        System.out.println(
//            "end execute id=" + taskInfo.getId() + " executorSize=" + executor.getActiveCount() + ", cost=" + (
//                System.currentTimeMillis() - t));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private ThreadPoolExecutor makeExecutePool() {
    return new ThreadPoolExecutor(Context.getConfig().getCorePoolSize(),
        Context.getConfig().getMaxPoolSize(),
        1,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(Context.getConfig().getQueueLength()),
        new DefaultThreadFactory(),
        new ThreadPoolExecutor.AbortPolicy());
  }

  private int select(TaskInfo taskInfo) {
    if (selector == null) {
      selector = PoolSelectorEnum.select(Context.getConfig().getPoolSelector());
    }
    return selector.select(taskInfo);
  }

  public ThreadPoolExecutor[] getExecutePools() {
    return executePools;
  }
}
