package com.jzp.task.revolver.handler;


import com.jzp.task.revolver.storage.TaskInfo;

/**
 * 线程池选择器
 */
public interface IPoolSelector {

  int select(TaskInfo taskInfo);
}
