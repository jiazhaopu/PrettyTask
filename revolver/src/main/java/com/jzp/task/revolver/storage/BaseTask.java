package com.jzp.task.revolver.storage;

import com.jzp.task.revolver.handler.ITaskHandler;

public class BaseTask {

  private String name;
  private Class<? extends ITaskHandler> handler;
  private String content;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Class<? extends ITaskHandler> getHandler() {
    return handler;
  }

  public void setHandler(Class<? extends ITaskHandler> handler) {
    this.handler = handler;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
