package com.jzp.task.aync;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class TaskProcessor implements ILogger {


  private ScheduledExecutorService scheduleService;

  public void init() {
//    if (State.RUNNING.equals(Context.getState().get())) {
//      return;
//    }
    Context.getState().compareAndSet(State.CREATE, State.RUNNING);
    scheduleService = Executors.newScheduledThreadPool(Context.getConfig().getSchedThreadNum(), new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        // TODO Auto-generated method stub
        Thread thread = new Thread(r,"MsgScheduledThread");
        return thread;
      }
    });
//    scheduleService.scheduleAtFixedRate(new ExecuteThread(), Context.getConfig().getExecutePeriod(),
//        Context.getConfig().getExecutePeriod(),
//        TimeUnit.MILLISECONDS);
    new TimeWheelThread().start();

    scheduleService.scheduleAtFixedRate(new ShardThread(), Context.getConfig().getShardPeriod(),
        Context.getConfig().getShardPeriod(), TimeUnit.MILLISECONDS);
  }

  public void close(){
    try {
      scheduleService.awaitTermination(Context.getConfig().getCloseWaitTime(), TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      logException("",e);
    }
    if(!scheduleService.isShutdown())
      scheduleService.shutdownNow();
  }
  public void put(TaskInfo taskInfo) {
    if (taskInfo == null) {
      return;
    }
    ConcurrentSkipListSet<Integer> queue = route(taskInfo.getNextTime());
    if (queue != null) {
      try {
        if (queue.contains(taskInfo.getId())){
          System.out.println("queue.containsId now="+new Date()+", sec="+ Context.getTimeWheelIndex(taskInfo.getNextTime()) +" taskInfo="+taskInfo.toString());
        }
        queue.add(taskInfo.getId());
//        System.out.println("put time = "+new Date()+", taskId="+taskInfo.getId()+", nextTime="+Context.getTimeWheelIndex(taskInfo.getNextTime()));
      } catch (Exception e) {
        logException(taskInfo.toString(),e);
      }
    }
  }



  private ConcurrentSkipListSet<Integer> route(long nextTime) {
    if (nextTime > System.currentTimeMillis() + Context.getConfig().getTimeWheelLength() * 1000){
      return null;
    }
    return Context.getWheelCluster(nextTime);
  }


  public void reloadTask(){
    long time = System.currentTimeMillis();
    long beforeNextTime = time + Context.getConfig().getTimeWheelLength() * 1000;
    try {
      // 查询出任务未结束，在未来 beforeNextTime 毫秒内需要执行的任务
      // 任务的下次执行时间 < 当前时间 + beforeNextTime
      List<TaskInfo> list = Context.getTaskStorage().selectWaitingBeforeNextTime(beforeNextTime);
      for (TaskInfo taskInfo : list) {
        System.out.println("scanAndPutTask taskInfo="+taskInfo.toString());
        // 如果该任务已经落后超过 5秒，需要重新计算下次触发时间
        if (taskInfo.getNextTime() < time - 5 * 1000){
          taskInfo.setNextTime(CronUtil.nextExecuteTime(taskInfo));
          Context.getTaskStorage().updateTask(taskInfo);
        }
        Context.getTaskProcessor().put(taskInfo);
      }
    }catch (Exception e){
      logException("",e);
    }
  }

}

