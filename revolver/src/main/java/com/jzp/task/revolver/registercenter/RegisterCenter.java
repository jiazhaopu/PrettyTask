package com.jzp.task.revolver.registercenter;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.executor.ThreadPoolHelper;
import com.jzp.task.revolver.handler.ILogger;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RegisterCenter implements ILogger {

  private static final String REVOLVER_PATH = "revolver";

  private static final String NODE_PATH = UUID.randomUUID().toString();

  protected ZookeeperClient zookeeperClient;

  public RegisterCenter(ZookeeperClient zookeeperClient) {
    this.zookeeperClient = zookeeperClient;
  }

  private String getRegisterPath(String product, String module) {
    if (StringUtils.isNotBlank(module)) {
      return ZookeeperClient.getPath(getRevolverPath(), product, module);
    } else if (StringUtils.isNotBlank(product)) {
      return ZookeeperClient.getPath(getRevolverPath(), product);
    } else {
      return getRevolverPath();
    }
  }

  public String getRevolverPath() {
    return ZookeeperClient.getPath(zookeeperClient.getZookeeperRootPath(), REVOLVER_PATH);
  }

  public String getProductPath() {
    return ZookeeperClient.getPath(getRevolverPath(), Context.getConfig().getProduct());
  }


  public String getModulePath() {
    return ZookeeperClient.getPath(getProductPath(), Context.getConfig().getModule());
  }

  public String getNodePath() {
    return ZookeeperClient.getPath(getModulePath(), NODE_PATH);
  }

  public void createRevolverPath() throws Exception {
    zookeeperClient.createPersistentPath(getRevolverPath());
  }

  public void createProductPath() throws Exception {
    zookeeperClient.createPersistentPath(getProductPath());
  }

  public void createModulePath() throws Exception {
    zookeeperClient.createPersistentPath(getModulePath());
  }

  public void createNodePathSetHost() {
    setDataForEphe(getNodePath(), Context.getHost());
//    zookeeperClient.createEphemeralPath(getNodePath());
  }

  public void registerNode() throws Exception {
    createRevolverPath();
    createProductPath();
    createModulePath();
    createNodePathSetHost();
  }

  public void beatsAndWatcher() throws Exception {
    ThreadPoolHelper.schedulePool.scheduleAtFixedRate(new BeatThread(), Context.getConfig().getBeatPeriod(),
        Context.getConfig().getBeatPeriod(), TimeUnit.MILLISECONDS);
    new Watcher().start(getModulePath());
  }

  public Boolean setDataForEphe(String path, String name) {
    try {
      zookeeperClient.setDataForEphe(path, name);
      return true;
    } catch (Exception e) {
      logException(name, e);
    }
    return false;
  }


  public List<String> getAvailableHost() {
    String path = Context.getRegisterCenter().getModulePath();
    List<String> list;
    try {
      list = zookeeperClient.getCuratorFramework().getChildren().forPath(path);
    } catch (Exception e) {
      logException(path, e);
      return null;
    }
    LOGGER.info("children for path. [path='{}', children='{}']", path, list);
    List<String> availableHost = new ArrayList<>();
    for (String children : list) {
      String childrenPath = ZookeeperClient.getPath(path, children);
      try {
        availableHost.add(zookeeperClient.getData(childrenPath));
      } catch (Exception e) {
        logException(path, e);
      }
    }
    return availableHost;
  }
}
