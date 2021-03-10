package com.jzp.task.revolver;

public class ShardThread implements ILogger, Runnable {

  @Override
  public void run() {
    Context.getTaskProcessor().reloadTask();
  }
}
