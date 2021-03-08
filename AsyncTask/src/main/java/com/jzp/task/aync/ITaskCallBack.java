package com.jzp.task.aync;

import java.sql.SQLException;

public interface ITaskCallBack {

  void call(TaskInfo taskInfo, boolean success) throws SQLException;
}
