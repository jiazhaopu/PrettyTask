package com.jzp.task.revolver.utils;

import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.model.TaskInfo;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class CronUtil {


  protected static final Logger log = LoggerFactory.getLogger(CronUtil.class);

  public static boolean checkCron(TaskInfo taskInfo) {
    if (StringUtils.isNotEmpty(taskInfo.getCron()) && !CronExpression.isValidExpression(taskInfo.getCron())) {
      log.error("cron is not valid. [taskInfo='{}']", taskInfo);
      return false;
    }
    if (ScheduleType.CRON.getCode().equals(taskInfo.getScheduleType()) && StringUtils.isEmpty(taskInfo.getCron())) {
      log.error("cron is not valid. [taskInfo='{}']", taskInfo);
      return false;
    }

    if (ScheduleType.FIXED_TIME.getCode().equals(taskInfo.getScheduleType())
        && StringUtils.isEmpty(taskInfo.getCron())
        && (taskInfo.getNextTime() == null
        || taskInfo.getNextTime() == 0)) {
      return false;
    }
    
    return true;
  }

  public static long nextExecuteTime(TaskInfo taskInfo) throws Exception {
    long resetTime = 0;
    if (StringUtils.isNotEmpty(taskInfo.getCron())) {
      CronExpression cronExpression = new CronExpression(taskInfo.getCron());
      Date nextDate = cronExpression.getNextValidTimeAfter(new Date());
      return nextDate.getTime();
    }
    if (ScheduleType.RETRY.getCode().equals(taskInfo.getScheduleType())) {
      return System.currentTimeMillis() + RandomUtils.nextInt(1, 5) * 1000;
    }
    if (ScheduleType.FIXED_TIME.getCode().equals(taskInfo.getScheduleType())) {
      if (taskInfo.getNextTime() == null || taskInfo.getNextTime() == 0) {
        throw new Exception("FIXED_TIME Task need cron or nextTime");
      }
      return taskInfo.getNextTime() == null ? 0 : taskInfo.getNextTime();
    }
//   System.out.println("resetTime="+resetTime+", sec="+Context.getTimeWheelIndex(resetTime)+", now="+(Context.getCurrentTimeWheelIndex()));
    return resetTime;
  }
}
