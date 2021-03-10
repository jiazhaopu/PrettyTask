package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.model.TaskInfo;

import java.sql.SQLException;

public interface ITaskCallBack {

  void call(TaskInfo taskInfo, boolean success) throws SQLException;
}
