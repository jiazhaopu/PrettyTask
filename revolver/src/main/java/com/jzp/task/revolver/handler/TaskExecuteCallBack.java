package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.constants.TaskStatus;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.log.ILogger;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.utils.CronUtil;

import java.util.Date;

public class TaskExecuteCallBack implements ITaskCallBack, ILogger {

  @Override
  public void call(TaskInfo taskInfo, boolean success) {

    LOGGER.info("TaskExecuteCallBack start. [taskId={}, now='{}', nextTime='{}']",
        taskInfo.getId(),new Date(),new Date(taskInfo.getNextTime()));

    taskInfo.setExecuteTimes(taskInfo.getExecuteTimes() + 1);
    taskInfo.setStatus(success ? TaskStatus.SUCCESS.getCode() : TaskStatus.FAIL.getCode());

    if (!success && taskInfo.getExecuteTimes() < taskInfo.getMaxExecuteTimes()) {
      // 重新计算时间
      try {
        taskInfo.setNextTime(CronUtil.nextExecuteTime(taskInfo));
      } catch (Exception e) {
        logException(taskInfo.toString(), e);
      }

      Context.getTaskProcessor().put(taskInfo);

      LOGGER.info("TaskExecuteCallBack put task. [taskId={}, taskInfo='{}', nextTime='{}']",
          taskInfo.getId(),taskInfo.toString(),new Date(taskInfo.getNextTime()));
    }
    Context.getTaskStorage().updateTask(taskInfo);
    LOGGER.info("TaskExecuteCallBack end. [taskId={}, success={}, nextTime={}]",
        taskInfo.getId(),success,new Date(taskInfo.getNextTime()));
  }
}
