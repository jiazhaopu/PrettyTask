package com.jzp.task.revolver.register;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.log.ILogger;

/**
 * Prevent the heartbeat from disconnecting when the system is suspended or the network is shaking, and the node is lost
 * rebuild node after recovery
 * <p>
 * 防止系统暂停或者网络抖动时断开心跳，丢失节点
 * 恢复之后重建节点
 */
public class BeatThread implements Runnable, ILogger {

  @Override
  public void run() {
    try {
      Context.getRegisterCenter().createNodePath();
//      System.out.println("BeatThread. " + new Date());
    } catch (Exception e) {
      logException("beatThread err", e);
    }
  }
}