package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.storage.TaskInfo;

public interface IPoolSelector {

  int select(TaskInfo taskInfo);
}
