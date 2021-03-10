package com.jzp.task.revolver.constants;

import com.jzp.task.revolver.handler.BalanceSelector;
import com.jzp.task.revolver.handler.HashSelector;
import com.jzp.task.revolver.handler.IPoolSelector;
import com.jzp.task.revolver.handler.LowLoadSelector;
import com.jzp.task.revolver.handler.RandomSelector;
import com.jzp.task.revolver.handler.RoundSelector;

public enum PoolSelectorEnum {

  RANDOM("random", new RandomSelector()),

  HASH("hash", new HashSelector()),

  ROUND("round", new RoundSelector()),

  BALANCE("balance", new BalanceSelector()),

  LOW_LOAD("lowLoad", new LowLoadSelector()),
  ;


  private final String name;

  private final IPoolSelector selector;

  PoolSelectorEnum(String name, IPoolSelector selector) {
    this.name = name;
    this.selector = selector;
  }

  public static IPoolSelector select(String name) {
    for (PoolSelectorEnum value : PoolSelectorEnum.values()) {
      if (value.name.equalsIgnoreCase(name)) {
        return value.selector;
      }
    }
    return HASH.selector;
  }

}
