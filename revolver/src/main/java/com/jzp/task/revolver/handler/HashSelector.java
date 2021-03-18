package com.jzp.task.revolver.handler;


import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.TaskInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * 根据任务名(第一优先级) 或者 handler hash到固定线程池，特点是任务所在线程池是固定的，可以控制任务的并发度，算法简单。
 * 一般情况下任务分布比较均匀，极端情况线程池利用率不均
 */
public class HashSelector implements IPoolSelector {

  @Override
  public int select(TaskInfo taskInfo) {
    String key = StringUtils.isEmpty(taskInfo.getName()) ? taskInfo.getHandler() : taskInfo.getName();
    return key.hashCode() % Context.getConfig().getExecutePoolsNum();
  }
}
