package com.jzp.task.revolver.registercenter;

import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.handler.ILogger;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ZookeeperClient implements AutoCloseable, DisposableBean {

  Logger log = LoggerFactory.getLogger(ILogger.class);

  /**
   * 注册角色超时时间 在zk里面使用
   */
  private static final int REGISTER_ROLE_RETRY_TIMES = 3;
  private static final int REGISTER_ROLE_TIMEOUT_SECONDS = 3;
  private static final String RECIPES = "recipes";
  private final String zookeeperRootPath;

  /**
   * curator客户端
   */
  private final CuratorFramework curatorFramework;

  /**
   * 存储所有的zk lock
   */
  private Map<String, InterProcessMutex> lockMap;
  private List<NodeCache> nodeCacheList = new ArrayList<>();


  public ZookeeperClient(Config config) {
    String zookeeperConnect = config.getZookeeperConnect();
    this.zookeeperRootPath = config.getZookeeperRootPath();
    int zookeeperSessionTimeout =
        Integer.parseInt(config.getZookeeperSessionTimeout());
    int zookeeperConnectionTimeout =
        Integer.parseInt(config.getZookeeperConnectionTimeout());
    int zookeeperBaseRetryInterval =
        Integer.parseInt(config.getZookeeperRetryInterval());
    int zookeeperRetryTimes =
        Integer.parseInt(config.getZookeeperRetryTimes());
    this.lockMap = new HashMap<>();

    this.curatorFramework =
        CuratorFrameworkFactory.builder()
            .connectString(zookeeperConnect)
            .sessionTimeoutMs(zookeeperSessionTimeout)
            .connectionTimeoutMs(zookeeperConnectionTimeout)
            .retryPolicy(new ExponentialBackoffRetry(zookeeperBaseRetryInterval, zookeeperRetryTimes))
            .build();
    this.curatorFramework.start();
    Long sessionId;
    try {
      sessionId = this.curatorFramework.getZookeeperClient()
          .getZooKeeper()
          .getSessionId();
    } catch (Exception e) {
      log.error("fail to get session id", e);
      throw new RuntimeException(e);
    }
    log.debug("curator start end. [sessionId={}]", sessionId);
  }

  public static String getPath(String first, String... paths) {
    return Paths.get(first, paths)
        .toString()
        .replace("\\", "/");
  }

  /**
   * 销毁和Zookeeper的连接
   */
  @Override
  public void close() throws Exception {
    for (NodeCache nodeCache : nodeCacheList) {
      nodeCache.close();
    }
    if (this.curatorFramework != null) {
      this.curatorFramework.close();
    }
  }

  /**
   * 修改某个path的内容
   */
  public void setData(String path, String data) throws Exception {
    if (this.curatorFramework.checkExists()
        .forPath(path) == null) {
      this.curatorFramework.create()
          .withMode(CreateMode.PERSISTENT)
          .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
          .forPath(path, data.getBytes(StandardCharsets.UTF_8));
    } else {
      this.curatorFramework.setData()
          .forPath(path, data.getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * 创建临时节点并写入值
   */
  public void setDataForEphe(String path, String data) throws Exception {
    if (this.curatorFramework.checkExists()
        .forPath(path) == null) {
      this.curatorFramework.create()
          .withMode(CreateMode.EPHEMERAL)
          .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
          .forPath(path, data.getBytes(StandardCharsets.UTF_8));
    } else {
      this.curatorFramework.setData()
          .forPath(path, data.getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * 删除某个path
   */
  public void deletePath(String path) throws Exception {
    try {
      this.curatorFramework.delete()
          .forPath(path);
    } catch (Exception e) {
      log.error("delete path fail. [path='{}']", path, e);
      throw e;
    }
  }

  /**
   * 删除某个path，包括子 path
   */
  public void deletePathRecursion(String path) throws Exception {
    try {
      this.curatorFramework.delete()
          .deletingChildrenIfNeeded()
          .forPath(path);
    } catch (Exception e) {
      log.error("deletePathChildrenIfNeeded fail. [path='{}']", path, e);
      throw e;
    }
  }

  /**
   * 创建某个path,如果path已经存在则直接返回
   */
  public void createPersistentPath(String path) throws Exception {
    if (this.checkExists(path)) {
      log.warn("create path existed: {}", path);
      return;
    }
    try {
      this.curatorFramework.create()
          .withMode(CreateMode.PERSISTENT)
          .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
          .forPath(path);
    } catch (Exception e) {
      log.error("create path:{} fail", path, e);
      throw e;
    }
  }

  /**
   * 创建某个临时path,如果path已经存在则直接返回
   */
  public void createEphemeralPath(String path) throws Exception {
    if (this.checkExists(path)) {
      log.warn("create path existed: {}", path);
      return;
    }
    try {
      this.curatorFramework.create()
          .withMode(CreateMode.EPHEMERAL)
          .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
          .forPath(path);
    } catch (Exception e) {
      log.error("create path:{} fail", path, e);
      throw e;
    }
  }

  /**
   * 判断某个path是否存在
   */
  public boolean checkExists(String path) throws Exception {
    try {
      return this.curatorFramework.checkExists()
          .forPath(path) != null;
    } catch (Exception e) {
      log.error("check exists path:{} fail", path, e);
      throw e;
    }
  }

  /**
   * 判断某个path是否存在
   */
  public boolean checkExists(String path, Watcher watcher) throws Exception {
    try {
      return this.curatorFramework.checkExists()
          .usingWatcher(watcher)
          .forPath(path) != null;
    } catch (Exception e) {
      log.error("check exists path:{} fail", path, e);
      throw e;
    }
  }

  public List<String> lsPath(String path) throws Exception {
    return this.curatorFramework.getChildren()
        .forPath(path);
  }

  public String getData(String path) throws Exception {
    return new String(this.curatorFramework.getData()
        .forPath(path), StandardCharsets.UTF_8);
  }

  public boolean tryRegisterModule(final String moduleName) {
    String moduleNameLower = moduleName.toLowerCase();
    String path = getPath(this.zookeeperRootPath, moduleNameLower);
    InterProcessMutex lock = new InterProcessMutex(this.curatorFramework, path);
    // 增加重试 不然可能被monitor探测的锁住了
    for (int i = 0; i != REGISTER_ROLE_RETRY_TIMES; ++i) {
      try {
        if (lock.acquire(REGISTER_ROLE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
          this.lockMap.put(path, lock);
          log.debug("register module success. [moduleName='{}']", moduleNameLower);
          return true;
        }
        this.lockMap.remove(path);
        lock.release();
      } catch (Exception e) {
        log.warn("register module failed and error occurred. [moduleName='{}']", moduleNameLower);
        log.warn("exception detail:", e);
      }
    }
    return false;
  }

  /**
   * 锁的父目录，在经典的 zookeeperRootPath 目录下的封装.
   */
  public void registerModule(final String moduleName) throws Exception {
    acquireInterProcessMutex(this.zookeeperRootPath, moduleName);
  }

  /**
   * 锁的父目录，在经典的 zookeeperRootPath 目录下的封装.
   *
   * @param moduleName
   * @throws Exception
   */
  public void releaseModule(final String moduleName) throws Exception {
    releaseInterProcessMutex(this.zookeeperRootPath, moduleName);
  }

  /**
   * 与 registerModule 的区别，是这个方法带 rootPath
   *
   * @param rootPath
   * @param mutexName
   * @throws Exception
   */
  public void acquireInterProcessMutex(String rootPath, final String mutexName) throws Exception {
    String mutexNameLower = mutexName.toLowerCase();
    String path = getPath(rootPath, mutexNameLower);
    InterProcessMutex lock = new InterProcessMutex(this.curatorFramework, path);
    // 增加重试 不然可能被monitor探测的锁住了
    for (int i = 0; i != REGISTER_ROLE_RETRY_TIMES; ++i) {
      if (lock.acquire(REGISTER_ROLE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        try {
          this.lockMap.put(path, lock);
          log.info("finished to acquire inter-process lock. [path={}]", path);
          return;
        } catch (Exception e) {
          log.warn("failed to acquire inter-process lock. [path={}]", path);
          log.warn("exception detail:", e);
        }
        this.lockMap.remove(path);
        lock.release();
      }
    }
    throw new Exception("failed to acquire inter-process lock. [path=" + path + "]");
  }

  /**
   * 可选父目录的释放 zk 锁
   *
   * @param rootPath
   * @param mutexName
   * @throws Exception
   */
  public void releaseInterProcessMutex(String rootPath, final String mutexName) throws Exception {
    String mutexNameLower = mutexName.toLowerCase();
    String path = getPath(rootPath, mutexNameLower);
    log.info("start to release inter-process lock. [path={}]", path);
    // 可能多次release，或者release的时候还没有register
    if (this.lockMap.containsKey(path)) {
      InterProcessMutex lock = this.lockMap.get(path);
      lock.release();
      this.lockMap.remove(path);
      log.info("finished to release inter-process lock. [path={}]", path);
    }
  }

  public void createEphemeralPathAndSetData(String ephemeralPath, String data) throws Exception {
    this.curatorFramework.create()
        .withMode(CreateMode.EPHEMERAL)
        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
        .forPath(ephemeralPath, data.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 获取curator framework的客户端
   */
  public CuratorFramework getCuratorFramework() {
    return this.curatorFramework;
  }

  /**
   * 获取zk的跟路径
   */
  public String getZookeeperRootPath() {
    return this.zookeeperRootPath;
  }

  /**
   * 使用唯一的 recipes key 来拼装一个 path，使用场景例如：拼装调度模块的分布式锁 path.
   *
   * @param productName
   * @param recipeKey
   * @return
   */
  public String getPathByRecipeKey(String productName, String recipeKey) {
    return Paths.get(zookeeperRootPath, productName, RECIPES, recipeKey)
        .toString();
  }

  /**
   * 获取一个锁的 Zk Path
   *
   * @param productName 产品线名如：sf
   * @param lockName    锁名，注意 name 不要重复了
   * @return
   */
  public String getLockPath(String productName, String lockName) {
    return getPathByRecipeKey(productName, lockName);
  }

  /**
   * 获取一个锁的 Zk Path，注意 name 不要重复了
   *
   * @deprecated 这个方法获取的是不带产品线的结果，从 sp 1.15 以后获取 zk 锁需要加上产品线信息
   * 且锁的路径变成 ${root_path}/${product_name}/recipes/${lock_name}
   */
  @Deprecated
  public String getLockPath(String lockName) {
    return Paths.get(zookeeperRootPath, "locks", lockName)
        .toString();
  }

  @Override
  public void destroy() throws Exception {
    log.info("destroy zookeeper client");
    close();
  }
}
