package com.jzp.task.revolver.registercenter;

import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.executor.ThreadPoolHelper;
import com.jzp.task.revolver.failover.FailOverItem;
import com.jzp.task.revolver.failover.FailOverProcess;
import com.jzp.task.revolver.handler.ILogger;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.concurrent.TimeUnit;

public class Watcher implements ILogger {

  public Watcher() {
  }

  public void start(String path) throws Exception {
    CuratorFramework curatorFramework = Context.getZookeeperClient().getCuratorFramework();
    PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, path, true);
    //调用start方法开始监听 ，设置启动模式为同步加载节点数据
    pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
    //添加监听器
    pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {

      @Override
      public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        LOGGER.info("node changed. [type={}, path='{}']", event.getType(), event.getData());
        Context.addFailOverQueue(new FailOverItem(event, 10, TimeUnit.SECONDS));
        ThreadPoolHelper.submitToFailOverPool(new FailOverProcess());
      }
    });
  }


}
