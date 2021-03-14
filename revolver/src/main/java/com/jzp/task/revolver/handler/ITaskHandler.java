package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.constants.ResultEnum;

public interface ITaskHandler {

  ResultEnum execute(String val) throws Exception;
}
