package com.jzp.task.aync;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutePools {

  private final int executePoolsNum = 5;

  private static final ExecutePools pool = new ExecutePools();

  private final ThreadPoolExecutor[] executePools;

  public static ExecutePools getInstance(){
    return pool;
  }

  private ExecutePools() {
    executePools = new ThreadPoolExecutor[executePoolsNum];
  }

  AtomicInteger atomicInteger = new AtomicInteger(0);

  TaskHandler taskHandler = new TaskHandler();

  private ThreadPoolExecutor getExecutor(TaskInfo taskInfo){
    int hash = hash(taskInfo);
    ThreadPoolExecutor executor = executePools[hash];
    if (executor == null){
      return initExecutor(hash);
    }
    return executor;
  }

  private synchronized ThreadPoolExecutor initExecutor(int hash){
    ThreadPoolExecutor executors = executePools[hash];
    if (executors == null){
      executePools[hash] = makeExecutePool();
      return executePools[hash];
    }
    return executors;
  }

  public void submit(final TaskInfo taskInfo, ITaskCallBack callable){
      ThreadPoolExecutor executor = getExecutor(taskInfo);
//      System.out.println("submit pool_queueSize="+executor.getQueue().size() + ", id="+taskInfo.getId());
      executor.execute(()-> {
//        ITaskHandler handler = (ITaskHandler)ApplicationContextHelper.getBean(taskInfo.getHandler());
        ITaskHandler handler = taskHandler;
        try {
          long t = System.currentTimeMillis();
//          System.out.println("start execute id="+taskInfo.getId()+" executorPoolSize="+executor.getActiveCount()+", pool_queueSize="+executor.getQueue().size());
          System.out.println("start execute taskInfo="+taskInfo.toString()+" nextTime="+new Date(taskInfo.getNextTime())+",nowDate="+new Date());
          boolean res =  handler != null && handler.execute(taskInfo.getContent());
          callable.call(taskInfo,res);
//          System.out.println("end execute id="+taskInfo.getId()+" executorSize="+executor.getActiveCount()+", cost="+(System.currentTimeMillis()-t));
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
  }

  private ThreadPoolExecutor  makeExecutePool() {
    return new ThreadPoolExecutor(Context.getConfig().getCorePoolSize(),
        Context.getConfig().getMaxPoolSize(),
        1,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000),
//        new SynchronousQueue<>(),
        new DefaultThreadFactory(),
        new ThreadPoolExecutor.AbortPolicy());
  }

  private int hash(TaskInfo taskInfo){
    return taskInfo.getHandler().hashCode() % executePoolsNum;
  }


  public static void main(String[] args) throws InterruptedException {
    ITaskCallBack taskCaller = new TaskExecuteCallBack();
    long t = System.currentTimeMillis();
    for (int i=0;i<100;i++){
      TaskInfo taskInfo = new TaskInfo();
      taskInfo.setId(i);
      taskInfo.setContent(i+"");
      taskInfo.setMaxExecuteTimes(1);
      taskInfo.setHandler(""+i);
      taskInfo.setScheduleType(ScheduleType.IMMEDIATELY.getCode());
      taskInfo.setNextTime(System.currentTimeMillis());
      ExecutePools.getInstance().submit(taskInfo,taskCaller);
    }
//    System.out.println("submit cost time="+(System.currentTimeMillis() - t));

//    System.out.println("execute cost:"+(System.currentTimeMillis() - t)+", atomicInteger="+ExecutePools.getInstance().atomicInteger.get());
  }
}
