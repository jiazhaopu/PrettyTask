package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.context.Context;

import java.util.Collections;
import java.util.List;

/**
 * 从所有可用节点随机选择一个
 */
public class RandomHost implements com.jzp.task.revolver.handler.IHostSelector {
  @Override
  public String select() {
    List<String> list = Context.getRegisterCenter().getAvailableHost();
    Collections.shuffle(list);
    return list.get(0);
  }
}
