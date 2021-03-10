package com.jzp.task.revolver.failover;

import com.jzp.task.revolver.Context;
import com.jzp.task.revolver.IPUtils;
import com.jzp.task.revolver.model.TaskInfo;
import com.jzp.task.revolver.register.RegisterCenter;
import com.jzp.task.revolver.register.ZookeeperClient;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FailOverProcess {

  public static void process() {
    ZookeeperClient client = Context.getZookeeperClient();
    String path = Context.getRegisterCenter().getModulePath();
    CuratorFramework curatorFramework = client.getCuratorFramework();
    System.out.println(Context.getDelayQueue().size());
    System.out.println(Context.getDelayQueue().isEmpty());
    Set<String> availableHost = new HashSet<>();
    while (!Context.getDelayQueue().isEmpty()) {
      try {
//        NodeCache nodeCache = client.registerNodeCache(path);
        RegisterCenter registerCenter = new RegisterCenter(client);
        Object o = registerCenter.getConfig(path);
        System.out.println("+++++" + o.toString() + ", path=" + path);
        String data = client.getData(path);
        System.out.println("++++ data=+" + data + ", path=" + path);
//        ChildData childData = nodeCache.getCurrentData();
//        String s = new String(childData.getData());
        System.out.println(Context.getDelayQueue().size());
        Context.getDelayQueue().take();
        List<String> list = curatorFramework.getChildren().forPath(path);
        System.out.println(list);
        for (String children : list) {
          String childrenPath = ZookeeperClient.getPath(path, children);
          o = registerCenter.getConfig(ZookeeperClient.getPath(path, children));
          System.out.println(
              "=== o=" + (o == null ? null : o.toString()) + ", path=" + path + children + ", childrenData="
                  + client.getData(childrenPath));
          availableHost.add(client.getData(childrenPath));
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    failover(availableHost);

  }


  private static List<Integer> failover(Set<String> availableHost) {

    List<Integer> list = new ArrayList<>();
    try {
      String ip = IPUtils.getHostAddress();
      // 最新的 host 列表
      if (availableHost.size() == 0) {
        return new ArrayList<>();
      }
      // 所有等待执行的Task
      List<TaskInfo> waitingTask = getWaitingTas();
      //只保留故障的Task
      waitingTask = waitingTask.stream().filter(e -> !availableHost.contains(e.getHost())).collect(Collectors.toList());
      // 转移到本机，最多转移数量
      // 故障任务平均分配
      int failoverMax = waitingTask.size() / availableHost.size() + 1;
      int failoverCount = 0;
      for (TaskInfo taskInfo : waitingTask) {
        System.out.println("failover waitingTask=" + taskInfo.toString());
        // 故障转移，把host 改成当前机器
        int row = Context.getTaskStorage().updateHost(taskInfo.getId(), taskInfo.getHost(), ip);
        if (row >= 1) {
          failoverCount++;
        }
        if (failoverCount > failoverMax) {
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
//      logException("",e);
    }
    return list;
  }


  private static List<TaskInfo> getWaitingTas() throws Exception {
    return Context.getTaskStorage().getWaitingTask();
  }

}
