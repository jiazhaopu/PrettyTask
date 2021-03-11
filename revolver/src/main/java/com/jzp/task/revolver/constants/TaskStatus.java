package com.jzp.task.revolver.constants;

public enum TaskStatus {


  SUSPEND(-1),
  NEW(0),
  FAIL(1),
  SUCCESS(2);

  TaskStatus(Integer code) {
    this.code = code;
  }

  private Integer code;

  public Integer getCode() {
    return code;
  }

  public static TaskStatus fromCode(Integer code) {
    for (TaskStatus value : TaskStatus.values()) {
      if (value.code.equals(code)) {
        return value;
      }
    }
    return null;
  }

  public static boolean needGoOn(Integer code) {
    TaskStatus status = fromCode(code);
    return NEW.equals(status) || FAIL.equals(status);
  }

  public static boolean canSuspend(Integer code) {
    return needGoOn(code);
  }
}
