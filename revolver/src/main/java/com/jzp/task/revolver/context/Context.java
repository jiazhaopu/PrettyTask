package com.jzp.task.revolver.context;

import com.jzp.task.revolver.TaskProcessor;
import com.jzp.task.revolver.constants.ServerState;
import com.jzp.task.revolver.failover.FailOverItem;
import com.jzp.task.revolver.register.RegisterCenter;
import com.jzp.task.revolver.register.ZookeeperClient;
import com.jzp.task.revolver.storage.TaskStorage;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Context {

  private static Config config = new Config();

  private static ZookeeperClient zookeeperClient;

  private static RegisterCenter registerCenter;

  private static final ConcurrentSkipListSet[] timeWheel = new ConcurrentSkipListSet[getConfig().getTimeWheelLength()];

  static {
    for (int i = 0; i < config.getTimeWheelLength(); i++) {
      timeWheel[i] = new ConcurrentSkipListSet<Integer>();
    }
  }

  private static AtomicReference<ServerState> state = new AtomicReference<>();

  private static TaskProcessor taskProcessor;

  private static TaskStorage taskStorage;

  private static final DelayQueue<FailOverItem> failOverDelayQueue = new DelayQueue<>();

  public static Config getConfig() {
    return config;
  }

  public static void setConfig(Config config) {
    Context.config = config;
  }

  public static AtomicReference<ServerState> getState() {
    return state;
  }

  public static void setState(AtomicReference<ServerState> state) {
    Context.state = state;
  }

  public static TaskProcessor getTaskProcessor() {
    return taskProcessor;
  }

  public static void setTaskProcessor(TaskProcessor taskProcessor) {
    Context.taskProcessor = taskProcessor;
  }

  public static ConcurrentSkipListSet<Integer>[] getTimeWheel() {
    return timeWheel;
  }

  public static ConcurrentSkipListSet<Integer> getWheelCluster(long timeStamp) {
    int index = getTimeWheelIndex(timeStamp);
    ConcurrentSkipListSet<Integer> queue = getTimeWheel()[index];
//    System.out.println("getWheelCluster now="+new Date() +" index="+index+" ,queue="+queue.size());
    return queue;
  }

  public static DelayQueue<FailOverItem> getDelayQueue() {
    return failOverDelayQueue;
  }

  public static void addFailOverQueue(FailOverItem item) {
    failOverDelayQueue.add(item);
  }

  public static int getTimeWheelIndex(long timeStamp) {
    return (int) (timeStamp / 1000 % config.getTimeWheelLength());
  }

  public static TaskStorage getTaskStorage() {
    return taskStorage;
  }

  public static void setTaskStorage(TaskStorage taskStorage) {
    Context.taskStorage = taskStorage;
  }

  public static ZookeeperClient getZookeeperClient() {
    return zookeeperClient;
  }

  public static void setZookeeperClient(ZookeeperClient zookeeperClient) {
    Context.zookeeperClient = zookeeperClient;
  }

  public static RegisterCenter getRegisterCenter() {
    return registerCenter;
  }

  public static void setRegisterCenter(RegisterCenter registerCenter) {
    Context.registerCenter = registerCenter;
  }
}

