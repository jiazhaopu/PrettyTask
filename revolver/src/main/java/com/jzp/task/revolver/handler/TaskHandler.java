package com.jzp.task.revolver.handler;

public class TaskHandler implements ITaskHandler {

  @Override
  public boolean execute(String val) throws Exception {

    Thread.sleep(1000);
    return false;
  }
}
