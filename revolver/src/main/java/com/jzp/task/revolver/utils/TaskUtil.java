package com.jzp.task.revolver.utils;

import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.storage.TaskInfo;

public class TaskUtil {

  public static void checkRegisterAndStart(TaskInfo taskInfo) throws Exception {

    if (!CronUtil.checkCron(taskInfo)) {
      throw new Exception("cron is not valid");
    }
    if (ScheduleType.RETRY.getCode().equals(taskInfo.getScheduleType())
        && taskInfo.getMaxExecuteTimes() == null) {
      throw new Exception("RETRY must set maxExecuteTimes");
    }

    if (taskInfo.getMaxExecuteTimes() != null && taskInfo.getExecuteTimes() >= taskInfo.getMaxExecuteTimes()) {
      throw new Exception("executeTimes is reach max");
    }

  }
}
