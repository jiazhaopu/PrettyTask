package com.jzp.task.revolver.executor;

import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.handler.ITaskCallBack;
import com.jzp.task.revolver.handler.ITaskHandler;
import com.jzp.task.revolver.handler.TaskExecuteCallBack;
import com.jzp.task.revolver.handler.TaskHandler;
import com.jzp.task.revolver.model.TaskInfo;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutePool {

  private static final ExecutePool pool = new ExecutePool();

  private final ThreadPoolExecutor[] executePools;

  public static ExecutePool getInstance() {
    return pool;
  }

  private ExecutePool() {
    executePools = new ThreadPoolExecutor[Context.getConfig().getExecutePoolsNum()];
  }
  
  TaskHandler taskHandler = new TaskHandler();

  private ThreadPoolExecutor getExecutor(TaskInfo taskInfo) {
    int hash = hash(taskInfo);
    ThreadPoolExecutor executor = executePools[hash];
    if (executor == null) {
      return initExecutor(hash);
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
//        ITaskHandler handler = (ITaskHandler)ApplicationContextHelper.getBean(taskInfo.getHandler());
      ITaskHandler handler = taskHandler;
      try {
        long t = System.currentTimeMillis();
//          System.out.println("start execute id="+taskInfo.getId()+" executorPoolSize="+executor.getActiveCount()+", pool_queueSize="+executor.getQueue().size());
//          System.out.println("start execute taskInfo="+taskInfo.toString()+" nextTime="+new Date(taskInfo.getNextTime())+",nowDate="+new Date());
        boolean res = handler != null && handler.execute(taskInfo.getContent());
        callable.call(taskInfo, res);
//          System.out.println("end execute id="+taskInfo.getId()+" executorSize="+executor.getActiveCount()+", cost="+(System.currentTimeMillis()-t));
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

  private int hash(TaskInfo taskInfo) {
    return taskInfo.getHandler().hashCode() % Context.getConfig().getExecutePoolsNum();
  }


  public static void main(String[] args) throws InterruptedException {
    ITaskCallBack taskCaller = new TaskExecuteCallBack();
    long t = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
      TaskInfo taskInfo = new TaskInfo();
      taskInfo.setId(i);
      taskInfo.setContent(i + "");
      taskInfo.setMaxExecuteTimes(1);
      taskInfo.setHandler("" + i);
      taskInfo.setScheduleType(ScheduleType.RETRY.getCode());
      taskInfo.setNextTime(System.currentTimeMillis());
      ExecutePool.getInstance().submit(taskInfo, taskCaller);
    }
//    System.out.println("submit cost time="+(System.currentTimeMillis() - t));

//    System.out.println("execute cost:"+(System.currentTimeMillis() - t)+", atomicInteger="+ExecutePools.getInstance().atomicInteger.get());
  }
}
