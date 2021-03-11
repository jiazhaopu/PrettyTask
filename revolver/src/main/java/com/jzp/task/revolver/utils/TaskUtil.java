package com.jzp.task.revolver.utils;

import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.constants.TaskStatus;
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

  public static boolean shouldRemove(TaskInfo taskInfo) {
    try {
      return !IPUtils.getHostAddress().equalsIgnoreCase(taskInfo.getHost()) ||
          taskInfo.getExecuteTimes() >= taskInfo.getMaxExecuteTimes() ||
          !TaskStatus.needGoOn(taskInfo.getStatus());
    } catch (Exception e) {
      return false;
    }
  }


  public static boolean shouldDo(long millSec1, long millSec2) {
    return millSec1 / 1000 <= millSec2 / 1000;
  }


}
