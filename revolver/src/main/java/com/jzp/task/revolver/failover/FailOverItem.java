package com.jzp.task.revolver.failover;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class FailOverItem implements Delayed {

  private long time;

  private PathChildrenCacheEvent event;

  public FailOverItem(PathChildrenCacheEvent event, long time, TimeUnit unit) {
    this.time = System.currentTimeMillis() + (time > 0 ? unit.toMillis(time) : 0);
    this.event = event;
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return time - System.currentTimeMillis();
  }

  @Override
  public int compareTo(Delayed o) {
    FailOverItem item = (FailOverItem) o;
    long diff = this.time - item.time;
    return diff <= 0 ? -1 : 1;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public PathChildrenCacheEvent getEvent() {
    return event;
  }

  public void setEvent(PathChildrenCacheEvent event) {
    this.event = event;
  }
}
