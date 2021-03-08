package com.jzp.task.aync;

public class Config {

  private int corePoolSize = Runtime.getRuntime().availableProcessors();

  private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;

  private int threadNum = 10;
  private int deleteTimePeriod = 180 ;//180s扫描一次表，删除历史消息
  private int deleteMsgOneTimeNum = 200; //一次删除200条消息
  private int schedThreadNum = 6;
  private int minIdleConnectionNum = 6;
  private int maxActiveConnectionNum = 20;
  private int sendMsgTimeout = 600;
  private int schedScanTimePeriod = 120 ;// 120s扫描一次，待发送的继续发送
  private int maxWaitTime = 6000; // mysql 连接池，拿连接的等待时间
  private int closeWaitTime = 5000;
  private int statsTimePeriod = 120 ;// 2分钟一次输出内部queue堆积的数量
  private int historyMsgStoreTime = 3;

  private int timeWheelLength = 60;

  // 5 ms
  private long executePeriod = 5;

  // 1 s
  private long timeWheelPeriod = 1000;

  // 5 min
  private long shardPeriod = 1000 * 60;

  private int currencyLevel = 100;

  public long getExecutePeriod() {
    return executePeriod;
  }

  public void setExecutePeriod(long executePeriod) {
    this.executePeriod = executePeriod;
  }

  public long getTimeWheelPeriod() {
    return timeWheelPeriod;
  }

  public void setTimeWheelPeriod(long timeWheelPeriod) {
    this.timeWheelPeriod = timeWheelPeriod;
  }

  public long getShardPeriod() {
    return shardPeriod;
  }

  public void setShardPeriod(long shardPeriod) {
    this.shardPeriod = shardPeriod;
  }

  public int getCurrencyLevel() {
    return currencyLevel;
  }

  public void setCurrencyLevel(int currencyLevel) {
    this.currencyLevel = currencyLevel;
  }

  public int getThreadNum() {
    return threadNum;
  }

  public void setThreadNum(int threadNum) {
    this.threadNum = threadNum;
  }

  public int getDeleteTimePeriod() {
    return deleteTimePeriod;
  }

  public void setDeleteTimePeriod(int deleteTimePeriod) {
    this.deleteTimePeriod = deleteTimePeriod;
  }

  public int getDeleteMsgOneTimeNum() {
    return deleteMsgOneTimeNum;
  }

  public void setDeleteMsgOneTimeNum(int deleteMsgOneTimeNum) {
    this.deleteMsgOneTimeNum = deleteMsgOneTimeNum;
  }

  public int getSchedThreadNum() {
    return schedThreadNum;
  }

  public void setSchedThreadNum(int schedThreadNum) {
    this.schedThreadNum = schedThreadNum;
  }

  public int getMinIdleConnectionNum() {
    return minIdleConnectionNum;
  }

  public void setMinIdleConnectionNum(int minIdleConnectionNum) {
    this.minIdleConnectionNum = minIdleConnectionNum;
  }

  public int getMaxActiveConnectionNum() {
    return maxActiveConnectionNum;
  }

  public void setMaxActiveConnectionNum(int maxActiveConnectionNum) {
    this.maxActiveConnectionNum = maxActiveConnectionNum;
  }

  public int getSendMsgTimeout() {
    return sendMsgTimeout;
  }

  public void setSendMsgTimeout(int sendMsgTimeout) {
    this.sendMsgTimeout = sendMsgTimeout;
  }

  public int getSchedScanTimePeriod() {
    return schedScanTimePeriod;
  }

  public void setSchedScanTimePeriod(int schedScanTimePeriod) {
    this.schedScanTimePeriod = schedScanTimePeriod;
  }

  public int getMaxWaitTime() {
    return maxWaitTime;
  }

  public void setMaxWaitTime(int maxWaitTime) {
    this.maxWaitTime = maxWaitTime;
  }

  public int getCloseWaitTime() {
    return closeWaitTime;
  }

  public void setCloseWaitTime(int closeWaitTime) {
    this.closeWaitTime = closeWaitTime;
  }

  public int getStatsTimePeriod() {
    return statsTimePeriod;
  }

  public void setStatsTimePeriod(int statsTimePeriod) {
    this.statsTimePeriod = statsTimePeriod;
  }

  public int getHistoryMsgStoreTime() {
    return historyMsgStoreTime;
  }

  public void setHistoryMsgStoreTime(int historyMsgStoreTime) {
    this.historyMsgStoreTime = historyMsgStoreTime;
  }

  public int getTimeWheelLength() {
    return timeWheelLength;
  }

  public void setTimeWheelLength(int timeWheelLength) {
    this.timeWheelLength = timeWheelLength;
  }

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }
}
