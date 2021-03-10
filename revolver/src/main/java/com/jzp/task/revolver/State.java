package com.jzp.task.revolver;

public enum State {

  /**
   * Service just created,not start
   */
  CREATE,
  /**
   * Service Running
   */
  RUNNING,
  /**
   * Service Closed
   */
  CLOSED,
  /**
   * Service failure
   */
  FAILED
}
