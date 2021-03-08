package com.jzp.task.aync;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShardThread extends Thread implements ILogger {

  @Override
  public void run() {
    failover();
    Context.getTaskProcessor().reloadTask();
  }

  private List<Integer> failover(){

    List<Integer> list = new ArrayList<>();
    try {
      String ip = IPUtils.getHostAddress();
      List<String> nowHostList = getNowHostList();
      // 最新的 host 列表
      if (nowHostList.size() == 0){
        return new ArrayList<>();
      }
      // 所有等待执行的Task
      List<TaskInfo> waitingTask = getWaitingTas();
      //只保留故障的Task
      waitingTask = waitingTask.stream().filter( e-> !nowHostList.contains(e.getHost())).collect(Collectors.toList());
      // 转移到本机，最多转移数量
      // 故障任务平均分配
      int failoverMax = waitingTask.size() / nowHostList.size() + 1;
      int failoverCount = 0;
      for (TaskInfo taskInfo : waitingTask) {
        System.out.println("failover waitingTask="+taskInfo.toString());
        // 故障转移，把host 改成当前机器
          int row = Context.getTaskStorage().updateHost(taskInfo.getId(),taskInfo.getHost(),ip);
          if (row >=1){
//            Context.getTaskProcessor().put(Context.getTaskStorage().getTaskById(taskInfo.getId()));
            failoverCount++;
          }
          if (failoverCount > failoverMax){
            break;
          }
      }
    }catch (Exception e){
      logException("",e);
    }
    return list;
  }



  private List<TaskInfo> getWaitingTas() throws Exception {
    return Context.getTaskStorage().getWaitingTask();
  }

  private List<String> getNowHostList(){
    return new ArrayList<>();
  }
}
