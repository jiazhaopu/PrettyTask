package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.constants.ResultEnum;
import com.jzp.task.revolver.constants.TaskStatus;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.utils.CronUtil;

import java.util.Date;
import java.util.Objects;

public class TaskExecuteCallBack implements ITaskCallBack, ILogger {

  @Override
  public void call(TaskInfo taskInfo, ResultEnum resultEnum) {

    LOGGER.info("TaskExecuteCallBack start. [taskId={}, now='{}', nextTime='{}']",
        taskInfo.getId(), new Date(), new Date(taskInfo.getNextTime()));

    TaskInfo update = new TaskInfo();
    update.setStatus(Objects.requireNonNull(TaskStatus.fromCode(resultEnum.getCode())).getCode());
    update.setId(taskInfo.getId());
    if (ResultEnum.CONTINUE.equals(resultEnum)
        && update.getExecuteTimes() < taskInfo.getMaxExecuteTimes() - 1) {
      // 重新计算时间
      try {
        update.setNextTime(CronUtil.nextExecuteTime(taskInfo));
      } catch (Exception e) {
        logException(taskInfo.toString(), e);
      }

      Context.getTaskProcessor().put(update);
      LOGGER.info("TaskExecuteCallBack put task. [taskId={}, taskInfo='{}', nextTime='{}']",
          taskInfo.getId(), taskInfo.toString(), new Date(taskInfo.getNextTime()));
    }
    try {
      Context.getTaskStorage().updateExecute(update);
      LOGGER.info("TaskExecuteCallBack end. [taskId={}, result={}, nextTime={}]",
          taskInfo.getId(), resultEnum, new Date(taskInfo.getNextTime()));
    } catch (Exception e) {
      logException(taskInfo.toString(), e);
    }

  }
}
