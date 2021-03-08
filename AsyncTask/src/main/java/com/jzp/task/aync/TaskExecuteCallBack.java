package com.jzp.task.aync;

import java.sql.SQLException;

public class TaskExecuteCallBack implements ITaskCallBack, ILogger {

  @Override
  public void call(TaskInfo taskInfo, boolean success) throws SQLException {

//    System.out.println("TaskCaller start id="+taskInfo.getId() + "now="+new Date() + "executeTime="+new Date(taskInfo.getNextTime()));

    taskInfo.setExecuteTimes(taskInfo.getExecuteTimes()+1);
    taskInfo.setStatus(success? TaskStatus.SUCCESS.getCode():TaskStatus.FAIL.getCode());

    if (!success && taskInfo.getExecuteTimes() < taskInfo.getMaxExecuteTimes()){
        // 重新计算时间
        try {
          taskInfo.setNextTime(CronUtil.nextExecuteTime(taskInfo));
        }catch (Exception e){
          logException(taskInfo.toString(),e);
        }
//      task.setNextTime(taskInfo.getNextTime());
//      System.out.println("TaskExecuteCallBack put taskInfo="+taskInfo.toString()+" now="+new Date()+", taskSec="+Context.getTimeWheelIndex(taskInfo.getNextTime()));
      Context.getTaskProcessor().put(taskInfo);
    }
    Context.getTaskStorage().updateTask(taskInfo);
//    System.out.println("TaskCaller end id="+taskInfo.getId()+", success="+success+" nextTime="+new Date(taskInfo.getNextTime()));
  }
}
