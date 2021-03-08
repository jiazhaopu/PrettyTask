package com.jzp.task.aync;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterCenter {


  Logger log = LoggerFactory.getLogger(ILogger.class);

  private static final String CONFIG_ROOT = "server";

  protected ZookeeperClient zookeeperClient;

  protected Map<String, Object> cacheMap = new ConcurrentHashMap<>();

  protected Map<String, NodeCache> zkNodeCacheMap = new ConcurrentHashMap<>();

  private <T> void updateConfigEntity(String metaPath, ChildData data, Class<T> configClass) throws IOException {
    T configEntity = null;
    if (data != null) {
//      configEntity = SensorsConstants.DEFAULT_OBJECT_MAPPER.readValue(data.getData(), configClass);
    }
    log.info("update config! [path='{}', value='{}']", metaPath, configEntity);
    cacheMap.put(metaPath, configEntity);
  }

  private synchronized <T> T getConfig(Class<T> configClass, String metaPath) throws Exception {
    if (!zkNodeCacheMap.containsKey(metaPath)) {
      NodeCache nodeCache = zookeeperClient.registerNodeCache(metaPath);
      nodeCache.getListenable().addListener(new NodeCacheListener() {
        @Override
        public void nodeChanged() throws Exception {
          updateConfigEntity(metaPath, nodeCache.getCurrentData(), configClass);
        }
      });
      zkNodeCacheMap.put(metaPath, nodeCache);
      updateConfigEntity(metaPath, nodeCache.getCurrentData(), configClass);
      log.info("add node cache. [path='{}', cacheSize={}]", metaPath, zkNodeCacheMap.size());
    }
    Object result = cacheMap.get(metaPath);
    if (result == null) {
      log.info("empty config. [path='{}']", metaPath);
      return null;
    }
    return (T) result;
  }

  private String getServerConfPath(String product, String applicationId) throws Exception {
    if (StringUtils.isNotBlank(applicationId)) {
      return ZookeeperClient.getPath(zookeeperClient.getZookeeperRootPath(), product, CONFIG_ROOT, applicationId);
    } else if (StringUtils.isNotBlank(product)) {
      return ZookeeperClient.getPath(zookeeperClient.getZookeeperRootPath(), product, CONFIG_ROOT);
    }

    throw new Exception(
        String.format("server conf path is invalid. [product=%s, applicationId=%s]", product, applicationId));
  }

  public <T> T getGlobalServerConfig(String product, Class<T> configClass) throws Exception {
    return getConfig(configClass, getServerConfPath(product, null));
  }

  public <T> T getServerConfig(String product, String applicationId, Class<T> configClass)
      throws Exception {
    return getConfig(configClass, getServerConfPath(product, applicationId));
  }

  public Boolean setServerConfig(String product, String applicationId, String configInfo) throws Exception {
    try {
      zookeeperClient.setData(getServerConfPath(product, applicationId), configInfo);
      return true;
    } catch (Exception e) {
      log.error("failed to set server config. [applicationId='{}', configInfo='{}']", applicationId, configInfo, e);
    }
    return false;
  }
}
