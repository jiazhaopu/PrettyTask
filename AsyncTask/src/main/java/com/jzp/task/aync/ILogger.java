package com.jzp.task.aync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ILogger {

 Logger LOGGER = LoggerFactory.getLogger(ILogger.class);

  default void logException(String msg, Exception e){
    String err = String.format("class=%s, exception='%s'",getClass().getSimpleName(),e.getMessage());
    LOGGER.error(msg,err,e.getMessage(),e);

  }


}
