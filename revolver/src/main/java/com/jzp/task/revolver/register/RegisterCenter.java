package com.jzp.task.revolver.register;

import com.jzp.task.revolver.Context;
import com.jzp.task.revolver.ILogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterCenter {


  Logger log = LoggerFactory.getLogger(ILogger.class);

  private static final String REVOLVER_PATH = "revolver";

  private static final String NODE_PATH = UUID.randomUUID().toString();

  protected ZookeeperClient zookeeperClient;

  protected Map<String, Object> cacheMap = new ConcurrentHashMap<>();

  protected Map<String, NodeCache> zkNodeCacheMap = new ConcurrentHashMap<>();

  private void updateConfigEntity(String metaPath, ChildData data) throws IOException {
    try {
      String configEntity = null;
      if (data != null) {
        configEntity = new String(data.getData());
      }
      log.info("update config! [path='{}', value='{}']", metaPath, configEntity);
      cacheMap.put(metaPath, configEntity);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public RegisterCenter(ZookeeperClient zookeeperClient) {
    this.zookeeperClient = zookeeperClient;
  }

  public synchronized Object getConfig(String metaPath) throws Exception {
    if (!zkNodeCacheMap.containsKey(metaPath)) {
      NodeCache nodeCache = zookeeperClient.registerNodeCache(metaPath);
//      nodeCache.getListenable().addListener(new NodeCacheListener() {
//        @Override
//        public void nodeChanged() throws Exception {
//
////          updateConfigEntity(metaPath, nodeCache.getCurrentData());
//          System.out.println("RegisterCenter nodeChanged, metaPath="+metaPath+", nodeCache.getCurrentData()="+nodeCache.getCurrentData());
////          Context.getDelayQueue().add()
//        }
//
//      });
      zkNodeCacheMap.put(metaPath, nodeCache);
      updateConfigEntity(metaPath, nodeCache.getCurrentData());
      log.info("add node cache. [path='{}', cacheSize={}]", metaPath, zkNodeCacheMap.size());
    }
    Object result = cacheMap.get(metaPath);
    if (result == null) {
      log.info("empty config. [path='{}']", metaPath);
      return null;
    }
    return result;
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

  public Object getServerConfig(String product, String applicationId)
      throws Exception {
    return getConfig(getRegisterPath(product, applicationId));
  }

  public Boolean setServerConfig(String product, String applicationId, String configInfo) throws Exception {
    try {
      zookeeperClient.setData(getRegisterPath(product, applicationId), configInfo);
      return true;
    } catch (Exception e) {
      log.error("failed to set server config. [applicationId='{}', configInfo='{}']", applicationId, configInfo, e);
    }
    return false;
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

  public void createNodePath() throws Exception {
    zookeeperClient.createEphemeralPath(getNodePath());
  }

  public void registerNode() throws Exception {
    createRevolverPath();
    createProductPath();
    createModulePath();
    createNodePath();
    createNodePath();
    createNodePath();
  }

  public Boolean setDataForEphe(String configInfo) {
    try {
      zookeeperClient.setDataForEphe(getNodePath(), configInfo);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
//      log.error("failed to set server config. [applicationId='{}', configInfo='{}']", applicationId, configInfo, e);
    }
    return false;
  }
}
