package com.jzp.task.revolver.storage;

public class RetryTask extends BaseTask {

  private String cron;

  private Integer maxExecuteTimes;

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public Integer getMaxExecuteTimes() {
    return maxExecuteTimes;
  }

  public void setMaxExecuteTimes(Integer maxExecuteTimes) {
    this.maxExecuteTimes = maxExecuteTimes;
  }
}
