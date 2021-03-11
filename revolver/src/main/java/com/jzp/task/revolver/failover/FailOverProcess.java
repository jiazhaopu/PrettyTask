package com.jzp.task.revolver.failover;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.register.ZookeeperClient;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.utils.IPUtils;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FailOverProcess implements Runnable {

  @Override
  public void run() {
    process();
  }

  private void process() {
    ZookeeperClient client = Context.getZookeeperClient();
    String path = Context.getRegisterCenter().getModulePath();
    CuratorFramework curatorFramework = client.getCuratorFramework();
    Set<String> availableHost = new HashSet<>();
    while (!Context.getDelayQueue().isEmpty()) {
      try {
        String data = client.getData(path);
        System.out.println("++++ data=+" + data + ", path=" + path);
        Context.getDelayQueue().take();
        List<String> list = curatorFramework.getChildren().forPath(path);
        System.out.println(list);
        for (String children : list) {
          String childrenPath = ZookeeperClient.getPath(path, children);
          System.out.println("path = " + path + children + ", childrenData = "
              + client.getData(childrenPath));
          availableHost.add(client.getData(childrenPath));
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    failover(availableHost);

  }


  private List<Integer> failover(Set<String> availableHost) {

    List<Integer> list = new ArrayList<>();
    try {
      String ip = IPUtils.getHostAddress();
      // 最新的 host 列表
      if (availableHost.size() == 0) {
        return new ArrayList<>();
      }
      // 所有等待执行的Task
      List<TaskInfo> waitingTask = getWaitingTaskExceptMy();
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


  private List<TaskInfo> getWaitingTaskExceptMy() throws Exception {
    return Context.getTaskStorage().getWaitingTaskExceptMy();
  }

}
