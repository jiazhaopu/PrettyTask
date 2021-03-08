package com.jzp.task.aync;

public interface ITaskHandler {

  boolean execute(String val) throws Exception;
}
