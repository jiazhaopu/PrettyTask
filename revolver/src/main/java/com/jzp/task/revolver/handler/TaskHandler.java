package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.constants.ResultEnum;

public class TaskHandler implements ITaskHandler, ILogger {

  @Override
  public ResultEnum execute(String val) throws Exception {

    LOGGER.info("execute TaskHandler. val=" + val);
    Thread.sleep(1000);
    return ResultEnum.CONTINUE;
  }
}
