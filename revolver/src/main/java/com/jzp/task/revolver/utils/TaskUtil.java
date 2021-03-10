package com.jzp.task.revolver.utils;

import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.storage.TaskInfo;

public class TaskUtil {

  public static boolean check(TaskInfo taskInfo) throws Exception {

    if (ScheduleType.RETRY.getCode().equals(taskInfo.getScheduleType())
        && taskInfo.getMaxExecuteTimes() == null) {
      throw new Exception("RETRY must set maxExecuteTimes");
    }
    return true;
  }
}
