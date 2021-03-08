package com.jzp.task.aync;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class CronUtil {


  protected static final Logger log = LoggerFactory.getLogger(CronUtil.class);

  public static boolean checkCron(TaskInfo taskInfo){
    if (StringUtils.isNotEmpty(taskInfo.getCron()) && !CronExpression.isValidExpression(taskInfo.getCron())){
      log.error("cron is not valid. [taskInfo='{}']",taskInfo);
      return false;
    }
    if (ScheduleType.SCHEDULE.getCode().equals(taskInfo.getScheduleType()) && StringUtils.isEmpty(taskInfo.getCron())){
      log.error("cron is not valid. [taskInfo='{}']",taskInfo);
     return false;
    }
    return true;
  }

  public static long nextExecuteTime(TaskInfo taskInfo) {
    long resetTime = 0;
    try {
      if (StringUtils.isNotEmpty(taskInfo.getCron())){
        CronExpression cronExpression = new CronExpression(taskInfo.getCron());
        Date nextDate = cronExpression.getNextValidTimeAfter(new Date());
        resetTime = nextDate.getTime();
      }
      if (ScheduleType.IMMEDIATELY.getCode().equals(taskInfo.getScheduleType())){
        return System.currentTimeMillis() + RandomUtils.nextInt(1,5) * 1000;
      }
      if (ScheduleType.FIXED_RATE.getCode().equals(taskInfo.getScheduleType())){
        return taskInfo.getNextTime() == null ? 0 : taskInfo.getNextTime();
      }

    }catch (Exception e){
      log.error(e.getMessage(),e);
    }


//   System.out.println("resetTime="+resetTime+", sec="+Context.getTimeWheelIndex(resetTime)+", now="+(Context.getCurrentTimeWheelIndex()));
    return resetTime;
  }
}
