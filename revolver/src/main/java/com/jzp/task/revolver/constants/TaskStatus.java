package com.jzp.task.revolver.constants;

public enum TaskStatus {


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
}
