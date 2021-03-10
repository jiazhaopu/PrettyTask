package com.jzp.task.revolver.storage;

import com.jzp.task.revolver.constants.TaskStatus;

import java.util.Date;

public class TaskInfo {

  private Integer id;

  private Integer scheduleType;

  private String cron;

  private String content;

  private String handler;

  private Integer executeTimes = 0;

  private Integer maxExecuteTimes;

  private Long nextTime;

  private String host;

  private Integer status = TaskStatus.NEW.getCode();

  private Date createTime = new Date();

  private Date updateTime;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getScheduleType() {
    return scheduleType;
  }

  public void setScheduleType(Integer scheduleType) {
    this.scheduleType = scheduleType;
  }

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getHandler() {
    return handler;
  }

  public void setHandler(String handler) {
    this.handler = handler;
  }

  public Integer getExecuteTimes() {
    return executeTimes;
  }

  public void setExecuteTimes(Integer executeTimes) {
    this.executeTimes = executeTimes;
  }

  public Integer getMaxExecuteTimes() {
    return maxExecuteTimes;
  }

  public void setMaxExecuteTimes(Integer maxExecuteTimes) {
    this.maxExecuteTimes = maxExecuteTimes;
  }

  public Long getNextTime() {
    return nextTime;
  }

  public void setNextTime(Long nextTime) {
    this.nextTime = nextTime;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public String toString() {
    return "TaskInfo{" +
        "id=" + id +
        ", scheduleType=" + scheduleType +
        ", cron='" + cron + '\'' +
        ", content='" + content + '\'' +
        ", handler='" + handler + '\'' +
        ", executeTimes=" + executeTimes +
        ", maxExecuteTimes=" + maxExecuteTimes +
        ", nextTime=" + nextTime +
        ", host='" + host + '\'' +
        ", status=" + status +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        '}';
  }
}

