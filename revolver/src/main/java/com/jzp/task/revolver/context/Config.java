package com.jzp.task.revolver.context;

import org.apache.commons.lang3.StringUtils;

public class Config {

  // ==================== mysql 配置开始 =================
  private int minIdleConnectionNum = 6;
  private int maxActiveConnectionNum = 20;
  private int schedScanTimePeriod = 120;// 120s扫描一次，待发送的继续发送
  private int maxWaitTime = 6000; // mysql 连接池，拿连接的等待时间
  // ==================== mysql 配置结束 =================

  // ==================== 线程池配置开始 ===================
  private int corePoolSize = Runtime.getRuntime().availableProcessors();

  private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;

  private int queueLength = 1000;

  private int executePoolsNum = 5;

  private int schedThreadNum = 2;

  private int closeWaitTime = 5000;

  private int timeWheelLength = 60;

  private long shardPeriod = 1000 * 60;
  // ==================== 线程池配置结束 ===================

  // ==================== zookeeper 注册中心配置开始 =======
  private String product;

  private String module;

  private String zookeeperConnect;

  private String zookeeperRootPath;

  private String zookeeperSessionTimeout = "20000";

  private String zookeeperConnectionTimeout = "5000";

  private String zookeeperRetryInterval = "1000";

  private String zookeeperRetryTimes = "3";

  // ==================== zookeeper 注册中心配置结束 =======

  public long getShardPeriod() {
    return shardPeriod;
  }

  public void setShardPeriod(long shardPeriod) {
    this.shardPeriod = shardPeriod;
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

  public void setCloseWaitTime(int closeWaitTime) {
    this.closeWaitTime = closeWaitTime;
  }

  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public String getZookeeperConnect() {
    return zookeeperConnect;
  }

  public void setZookeeperConnect(String zookeeperConnect) {
    this.zookeeperConnect = zookeeperConnect;
  }

  public String getZookeeperRootPath() {
    return zookeeperRootPath;
  }

  public void setZookeeperRootPath(String zookeeperRootPath) {
    this.zookeeperRootPath = zookeeperRootPath;
  }

  public String getZookeeperSessionTimeout() {
    return zookeeperSessionTimeout;
  }

  public void setZookeeperSessionTimeout(String zookeeperSessionTimeout) {
    this.zookeeperSessionTimeout = zookeeperSessionTimeout;
  }

  public String getZookeeperConnectionTimeout() {
    return zookeeperConnectionTimeout;
  }

  public void setZookeeperConnectionTimeout(String zookeeperConnectionTimeout) {
    this.zookeeperConnectionTimeout = zookeeperConnectionTimeout;
  }

  public String getZookeeperRetryInterval() {
    return zookeeperRetryInterval;
  }

  public void setZookeeperRetryInterval(String zookeeperRetryInterval) {
    this.zookeeperRetryInterval = zookeeperRetryInterval;
  }

  public String getZookeeperRetryTimes() {
    return zookeeperRetryTimes;
  }

  public void setZookeeperRetryTimes(String zookeeperRetryTimes) {
    this.zookeeperRetryTimes = zookeeperRetryTimes;
  }

  public int getBeatPeriod() {
    if (StringUtils.isNotEmpty(zookeeperSessionTimeout)) {
      return Integer.parseInt(zookeeperSessionTimeout) * 2 / 3;
    }
    return 1000;
  }

  public int getExecutePoolsNum() {
    return executePoolsNum;
  }

  public void setExecutePoolsNum(int executePoolsNum) {
    this.executePoolsNum = executePoolsNum;
  }

  public int getQueueLength() {
    return queueLength;
  }

  public void setQueueLength(int queueLength) {
    this.queueLength = queueLength;
  }
}
