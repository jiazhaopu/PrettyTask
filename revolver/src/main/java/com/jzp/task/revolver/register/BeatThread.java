package com.jzp.task.revolver.register;

import com.jzp.task.revolver.Context;
import com.jzp.task.revolver.ILogger;

import java.util.Date;

/**
 * 防止假死之后，zookeeper心跳停止丢失节点
 * 恢复之后需要重建节点
 */
public class BeatThread implements Runnable, ILogger {

  @Override
  public void run() {
    try {
      Context.getRegisterCenter().createNodePath();
      System.out.println("BeatThread. " + new Date());
    } catch (Exception e) {
      logException("beatThread err", e);
    }
  }
}