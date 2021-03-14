package com.jzp.task.revolver.constants;

import java.util.Arrays;
import java.util.List;

public enum TaskStatus {


  SUSPEND(-1),
  NEW(0),
  FAIL(ResultEnum.CONTINUE.getCode()),
  SUCCESS(ResultEnum.FINISH.getCode());

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
    return needGoOn().contains(code);
  }

  public static List<Integer> needGoOn() {
    return Arrays.asList(NEW.code, FAIL.code);
  }

  public static boolean canSuspend(Integer code) {
    return needGoOn(code);
  }

  public static boolean canStart(Integer code) {
    return SUSPEND.code.equals(code);
  }

}
