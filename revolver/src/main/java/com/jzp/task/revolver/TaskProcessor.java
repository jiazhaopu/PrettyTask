package com.jzp.task.revolver;

import com.jzp.task.revolver.constants.ServerState;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.executor.ReloadAndShardThread;
import com.jzp.task.revolver.executor.ThreadPoolHelper;
import com.jzp.task.revolver.executor.TimeWheelThread;
import com.jzp.task.revolver.handler.ILogger;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.utils.CronUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class TaskProcessor implements ILogger {

  public void init() {
    Context.getState().compareAndSet(ServerState.CREATE, ServerState.RUNNING);
    new TimeWheelThread().start();
    ThreadPoolHelper.schedulePool.scheduleAtFixedRate(new ReloadAndShardThread(), Context.getConfig().getShardPeriod(),
        Context.getConfig().getShardPeriod(), TimeUnit.MILLISECONDS);
  }

  public void close() {
    try {
      ThreadPoolHelper.schedulePool.awaitTermination(Context.getConfig().getCloseWaitTime(), TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      logException("", e);
    }
    if (!ThreadPoolHelper.schedulePool.isShutdown())
      ThreadPoolHelper.schedulePool.shutdownNow();
  }

  public void put(TaskInfo taskInfo) {
    if (taskInfo == null) {
      return;
    }
    ConcurrentSkipListSet<Integer> queue = route(taskInfo.getNextTime());
    if (queue != null) {
      try {
        if (queue.contains(taskInfo.getId())) {
          LOGGER.info("queue containsId. [now={}, nextTime={}, taskId={}]",
              new Date(), Context.getTimeWheelIndex(taskInfo.getNextTime()), taskInfo.getId());
        } else {
          queue.add(taskInfo.getId());
          LOGGER.info("put taskId to queue. [now={}, nextTime={}, taskId={}]",
              new Date(), Context.getTimeWheelIndex(taskInfo.getNextTime()), taskInfo.getId());
        }
      } catch (Exception e) {
        logException(taskInfo.toString(), e);
      }
    }
  }

  public void remove(TaskInfo taskInfo) {
    if (taskInfo == null) {
      return;
    }
    ConcurrentSkipListSet<Integer> queue = route(taskInfo.getNextTime());
    if (queue != null) {
      queue.remove(taskInfo.getId());
    }
  }

  private ConcurrentSkipListSet<Integer> route(long nextTime) {
    if (nextTime > System.currentTimeMillis() + Context.getConfig().getTimeWheelLength() * 1000) {
      return null;
    }
    return Context.getWheelCluster(nextTime);
  }

  public void reloadTask() {
    long time = System.currentTimeMillis();
    long beforeNextTime = time + Context.getConfig().getTimeWheelLength() * 1000;
    try {
      // ???????????????????????????????????? beforeNextTime ??????????????????????????????
      // ??????????????????????????? < ???????????? + beforeNextTime
      List<TaskInfo> list = Context.getTaskStorage().selectMyWaitingBeforeNextTime(beforeNextTime);
      for (TaskInfo taskInfo : list) {
        // ????????????????????????????????? 5??????????????????????????????????????????
        if (taskInfo.getNextTime() < time - 5 * 1000) {
          taskInfo.setNextTime(CronUtil.nextExecuteTime(taskInfo));
          Context.getTaskStorage().updateNextTime(taskInfo.getId(), taskInfo.getNextTime());
        }
        Context.getTaskProcessor().put(taskInfo);
      }
    } catch (Exception e) {
      logException("", e);
    }
  }

}

