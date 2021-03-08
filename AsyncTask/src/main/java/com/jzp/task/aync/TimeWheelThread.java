package com.jzp.task.aync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class TimeWheelThread extends Thread implements ILogger {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private ITaskCallBack taskCaller = new TaskExecuteCallBack();

  public void run() {
    System.out.println("TimeWheelThread State="+ Context.getState().get());
      while (com.jzp.task.aync.State.RUNNING.equals(Context.getState().get())){
        sleep();
        long time  = System.currentTimeMillis();
        ConcurrentSkipListSet<Integer> queue =  Context.getWheelCluster(time);
        List<Integer> list = new ArrayList<>();
        while (!queue.isEmpty()){
          list.add(queue.pollFirst());
        }
        System.out.println(" while before poll listSize="+list.size()+"， queueSize="+queue.size()+" ,nowSec="+Context.getTimeWheelIndex(time)+"， now="+new Date(time));
//        System.out.println("sec = "+Context.getCurrentTimeWheelIndex()+" after poll size="+queue.size()+", task="+Context.getTimeWheelIndex(task.getNextTime()));
//        System.out.println(" before while poll size="+queue.size()+"， empty="+queue.isEmpty());
        for (Integer id : list) {
//          if (isSameSecond(task.getNextTime(),time)) {
//            System.out.println("TimeWheelThread do task="+id+" nowSec="+Context.getTimeWheelIndex(time));
            // select TaskInfo
            TaskInfo taskInfo = Context.getTaskStorage().getTaskById(id);
            // 判断是否应该执行
            if (shouldRemove(taskInfo)){
              System.out.println("remove task="+id+" ,ip="+taskInfo.getHost()+" nowSec="+Context.getTimeWheelIndex(time));
              continue;
            }
            boolean shouldDo = shouldDo(taskInfo.getNextTime(),time);
            if (shouldDo) {
//              System.out.println("shouldDo id="+id+", taskSec="+Context.getTimeWheelIndex(time)+", nowSec="+Context.getTimeWheelIndex(time));
              try {
                ExecutePools.getInstance().submit(taskInfo,taskCaller);
              }catch (Exception e){
                System.out.println(taskInfo.toString());
                e.printStackTrace();
                logException(taskInfo.toString(),e);
                taskInfo.setNextTime(CronUtil.nextExecuteTime(taskInfo));
                Context.getTaskStorage().updateTask(taskInfo);
                Context.getTaskProcessor().put(taskInfo);
              }

            }else {
              System.out.println("not do reput now="+new Date(time)+", taskNextDateTime="+new Date(taskInfo.getNextTime()) +" taskNextTime="+taskInfo.getNextTime()+", time="+time+
                  ", taskSec="+Context.getTimeWheelIndex(taskInfo.getNextTime())+" , nowSec="+Context.getTimeWheelIndex(time));
              Context.getTaskProcessor().put(taskInfo);
            }
//          }else {
//            System.out.println("reput task");
//            Context.getTaskProcessor().put(task);
//          }
        }
      }

    }

    private void sleep(){
      try {
        long sleep = 1000 - System.currentTimeMillis() % 1000;
        TimeUnit.MILLISECONDS.sleep(sleep);
      } catch (InterruptedException e) {
        log.error(e.getMessage(), e);
      }
    }

    private boolean shouldRemove(TaskInfo taskInfo){
      try {
        return !IPUtils.getHostAddress().equalsIgnoreCase(taskInfo.getHost()) ||
            taskInfo.getExecuteTimes()>=taskInfo.getMaxExecuteTimes() ||
            TaskStatus.SUCCESS.getCode().equals(taskInfo.getStatus());
      }catch (Exception e){
        return false;
      }
    }
    private boolean shouldDo(TaskInfo taskInfo,long time){
      return taskInfo.getNextTime()/1000 == time/1000;
    }

    private boolean shouldDo(long millSec1 ,long millSec2){
      return millSec1 /1000 <= millSec2 /1000;
    }
}
