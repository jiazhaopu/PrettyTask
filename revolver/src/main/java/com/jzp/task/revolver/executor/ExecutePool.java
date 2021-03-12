package com.jzp.task.revolver.executor;

import com.jzp.task.revolver.constants.PoolSelectorEnum;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.handler.IPoolSelector;
import com.jzp.task.revolver.handler.ITaskCallBack;
import com.jzp.task.revolver.handler.ITaskHandler;
import com.jzp.task.revolver.log.ILogger;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.utils.ApplicationContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutePool {

  Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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
        ,executor.getQueue().size(),taskInfo.getId(),executor.getActiveCount());
    executor.execute(() -> {
      try {
        ITaskHandler handler = (ITaskHandler) ApplicationContextHelper.getBean(taskInfo.getHandler());
        if (handler == null) {
          return;
        }
        long t = System.currentTimeMillis();
        LOGGER.info("start execute. [taskInfo='{}', nextTime={}, nowDate={}]",
            taskInfo.toString() , new Date(taskInfo.getNextTime()), new Date());
        boolean res = handler.execute(taskInfo.getContent());
        callable.call(taskInfo, res);
        LOGGER.info(
            "end execute. [id={}, executorSize={}, cost={} ms",
            taskInfo.getId(),executor.getActiveCount(), (System.currentTimeMillis() - t));
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
