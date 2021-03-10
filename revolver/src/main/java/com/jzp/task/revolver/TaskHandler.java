package com.jzp.task.revolver;

public class TaskHandler implements ITaskHandler {

  @Override
  public boolean execute(String val) throws Exception {

    Thread.sleep(1000);
//    return System.currentTimeMillis() % 2==0;
    return false;
  }
}
