package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.constants.ResultEnum;
import com.jzp.task.revolver.storage.TaskInfo;

public interface ITaskCallBack {

  void call(TaskInfo taskInfo, ResultEnum result);
}
