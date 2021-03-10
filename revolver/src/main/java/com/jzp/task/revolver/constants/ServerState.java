package com.jzp.task.revolver.constants;

public enum ServerState {

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
