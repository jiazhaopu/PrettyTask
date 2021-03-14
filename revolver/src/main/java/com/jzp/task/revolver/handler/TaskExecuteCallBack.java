package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.constants.ResultEnum;
import com.jzp.task.revolver.constants.TaskStatus;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.log.ILogger;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.utils.CronUtil;

import java.util.Date;
import java.util.Objects;

public class TaskExecuteCallBack implements ITaskCallBack, ILogger {

  @Override
  public void call(TaskInfo taskInfo, ResultEnum resultEnum) {

    LOGGER.info("TaskExecuteCallBack start. [taskId={}, now='{}', nextTime='{}']",
        taskInfo.getId(), new Date(), new Date(taskInfo.getNextTime()));

    taskInfo.setExecuteTimes(taskInfo.getExecuteTimes() + 1);
    taskInfo.setStatus(Objects.requireNonNull(TaskStatus.fromCode(resultEnum.getCode())).getCode());

    if (ResultEnum.CONTINUE.equals(resultEnum)
        && taskInfo.getExecuteTimes() < taskInfo.getMaxExecuteTimes()) {
      // 重新计算时间
      try {
        taskInfo.setNextTime(CronUtil.nextExecuteTime(taskInfo));
      } catch (Exception e) {
        logException(taskInfo.toString(), e);
      }

      Context.getTaskProcessor().put(taskInfo);
      LOGGER.info("TaskExecuteCallBack put task. [taskId={}, taskInfo='{}', nextTime='{}']",
          taskInfo.getId(), taskInfo.toString(), new Date(taskInfo.getNextTime()));
    }
    try {
      Context.getTaskStorage().updateTask(taskInfo);
      LOGGER.info("TaskExecuteCallBack end. [taskId={}, success={}, nextTime={}]",
          taskInfo.getId(), resultEnum, new Date(taskInfo.getNextTime()));
    } catch (Exception e) {
      logException(taskInfo.toString(), e);
    }

  }
}
