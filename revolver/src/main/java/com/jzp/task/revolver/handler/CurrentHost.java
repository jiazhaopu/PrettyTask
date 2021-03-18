package com.jzp.task.revolver.handler;


import com.jzp.task.revolver.context.Context;

/**
 * 选择当前host
 */
public class CurrentHost implements IHostSelector {
  @Override
  public String select() {
    return Context.getHost();
  }
}
