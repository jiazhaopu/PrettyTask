package com.jzp.task.revolver.constants;

public enum ResultEnum {

  CONTINUE(1), FINISH(2);

  private Integer code;

  ResultEnum(Integer code) {
    this.code = code;
  }

  public Integer getCode() {
    return code;
  }
}
