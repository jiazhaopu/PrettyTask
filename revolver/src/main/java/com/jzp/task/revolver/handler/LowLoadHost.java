package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.context.Context;

import java.util.List;
import java.util.Map;

/**
 * 从所有可用节点，选择任务最少的
 */
public class LowLoadHost implements IHostSelector {

  @Override
  public String select() throws Exception {
    List<String> availableHost = Context.getRegisterCenter().getAvailableHost();
    Map<String, Integer> waitingHost = Context.getTaskStorage().countWaitingHost();
    String lowHost = null;
    Integer lowCount = null;
    for (String host : availableHost) {
      int count = waitingHost.get(host);
      if (lowCount == null || count < lowCount) {
        lowHost = host;
        lowCount = count;
      }
    }
    return lowHost;
  }
}
