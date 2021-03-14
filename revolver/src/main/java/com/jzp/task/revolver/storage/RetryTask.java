package com.jzp.task.revolver.storage;

public class RetryTask extends BaseTask {

  private String cron;
  private Integer maxTimes;

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public Integer getMaxTimes() {
    return maxTimes;
  }

  public void setMaxTimes(Integer maxTimes) {
    this.maxTimes = maxTimes;
  }
}
