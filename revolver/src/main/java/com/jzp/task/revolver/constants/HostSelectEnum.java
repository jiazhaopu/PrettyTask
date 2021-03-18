package com.jzp.task.revolver.constants;

import com.jzp.task.revolver.handler.CurrentHost;
import com.jzp.task.revolver.handler.IHostSelector;
import com.jzp.task.revolver.handler.LowLoadHost;
import com.jzp.task.revolver.handler.RandomHost;


public enum HostSelectEnum {


  CURRENT("current", new CurrentHost()),
  RANDOM("random", new RandomHost()),
  LOW_LOAD("lowLoad", new LowLoadHost());


  private final String name;

  private final IHostSelector selector;

  HostSelectEnum(String name, IHostSelector selector) {
    this.name = name;
    this.selector = selector;
  }

  public static IHostSelector select(String name) {
    for (com.jzp.task.revolver.constants.HostSelectEnum value : com.jzp.task.revolver.constants.HostSelectEnum.values()) {
      if (value.name.equalsIgnoreCase(name)) {
        return value.selector;
      }
    }
    return CURRENT.selector;
  }

}
