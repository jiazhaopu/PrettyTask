package com.jzp.task.aync;

public enum ScheduleType {

  IMMEDIATELY(1),

  SCHEDULE(2),

  FIXED_RATE(3);

  private final Integer code;

  ScheduleType(Integer code) {
    this.code = code;
  }

  public Integer getCode() {
    return code;
  }
}
