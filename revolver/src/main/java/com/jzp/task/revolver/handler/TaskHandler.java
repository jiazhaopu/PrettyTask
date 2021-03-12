package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.log.ILogger;

public class TaskHandler implements ITaskHandler, ILogger {

  @Override
  public boolean execute(String val) throws Exception {

    LOGGER.info("execute TaskHandler. val=" + val);
    Thread.sleep(1000);
    return false;
  }
}
