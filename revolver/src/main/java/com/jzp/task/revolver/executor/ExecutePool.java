package com.jzp.task.revolver.executor;

import com.jzp.task.revolver.constants.PoolSelectorEnum;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.handler.HandlerContainer;
import com.jzp.task.revolver.handler.IPoolSelector;
import com.jzp.task.revolver.handler.ITaskCallBack;
import com.jzp.task.revolver.handler.ITaskHandler;
import com.jzp.task.revolver.log.ILogger;
import com.jzp.task.revolver.storage.TaskInfo;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutePool implements ILogger {

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
    LOGGER.info("submit. [pool_queueSize={}, taskId={}, activeCount={}]"
        , executor.getQueue().size(), taskInfo.getId(), executor.getActiveCount());
    executor.execute(() -> {
      boolean res = false;
      try {
        ITaskHandler handler = HandlerContainer.getBean(taskInfo.getHandler());
        if (handler != null) {
          long t = System.currentTimeMillis();
          LOGGER.info("start execute. [taskInfo='{}', nextTime={}, nowDate={}]",
              taskInfo.toString(), new Date(taskInfo.getNextTime()), new Date());
          res = handler.execute(taskInfo.getContent());
          LOGGER.info(
              "end execute. [id={}, executorSize={}, cost={} ms",
              taskInfo.getId(), executor.getActiveCount(), (System.currentTimeMillis() - t));
        }
      } catch (Exception e) {
        logException(taskInfo.toString(), e);
      }
      callable.call(taskInfo, res);
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
