package com.jzp.task.revolver;

public enum ScheduleType {

  RETRY(1),

  CRON(2),

  FIXED_TIME(3);

  private final Integer code;

  ScheduleType(Integer code) {
    this.code = code;
  }

  public Integer getCode() {
    return code;
  }
}
