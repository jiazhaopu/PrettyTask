package com.jzp.task.revolver.utils;

import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.constants.TaskStatus;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.CronTask;
import com.jzp.task.revolver.storage.FixedTask;
import com.jzp.task.revolver.storage.RetryTask;
import com.jzp.task.revolver.storage.TaskInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskUtil {


  static Logger LOGGER = LoggerFactory.getLogger(TaskUtil.class);

  public static void check(CronTask cronTask) throws Exception {
    if (StringUtils.isEmpty(cronTask.getCron())) {
      throw new Exception("cron 不能为空");
    }
  }

  public static void check(FixedTask fixedTask) throws Exception {
    if (fixedTask.getExecuteTime() == 0) {
      throw new Exception("executeTime 不能为空");
    }
  }


  public static void check(RetryTask retryTask) throws Exception {
    if (retryTask.getMaxExecuteTimes() == null || retryTask.getMaxExecuteTimes() < 1) {
      throw new Exception("maxExecuteTimes 必须大于 0");
    }
  }

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
      boolean b = !Context.getHost().equalsIgnoreCase(taskInfo.getHost()) ||
          taskInfo.getExecuteTimes() >= taskInfo.getMaxExecuteTimes() ||
          !TaskStatus.needGoOn(taskInfo.getStatus());
      if (b) {
        LOGGER.warn("remove. [taskId={}, ip='{}', myIp='{}', myHost-'{}', nowSec={}",
            taskInfo.getId(), taskInfo.getHost(), Context.getHost(), IPUtils.getHost(),
            Context.getTimeWheelIndex(taskInfo.getNextTime()));
      }
      return b;
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean shouldDo(long millSec1, long millSec2) {
    return millSec1 / 1000 <= millSec2 / 1000;
  }


}
