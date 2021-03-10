package com.jzp.task.revolver.executor;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.log.ILogger;

public class ShardThread implements ILogger, Runnable {

  @Override
  public void run() {
    Context.getTaskProcessor().reloadTask();
  }
}
