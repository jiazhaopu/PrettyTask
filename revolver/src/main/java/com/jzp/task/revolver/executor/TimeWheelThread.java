package com.jzp.task.revolver.executor;

import com.jzp.task.revolver.constants.ServerState;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.handler.ITaskCallBack;
import com.jzp.task.revolver.handler.TaskExecuteCallBack;
import com.jzp.task.revolver.log.ILogger;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.utils.CronUtil;
import com.jzp.task.revolver.utils.TaskUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class TimeWheelThread extends Thread implements ILogger {

  private final ITaskCallBack taskCaller = new TaskExecuteCallBack();

  public void run() {
    while (ServerState.RUNNING.equals(Context.getState().get())) {
      sleepToNextSecond();
      long time = System.currentTimeMillis();
      ConcurrentSkipListSet<Integer> queue = Context.getWheelCluster(time);
      List<Integer> list = new ArrayList<>();
      while (!queue.isEmpty()) {
        list.add(queue.pollFirst());
      }
      LOGGER.info("TimeWheelThread poll. [listSize={}, taskSec={}, nowDate{}",
          list.size(), Context.getTimeWheelIndex(time), new Date(time));
      for (Integer id : list) {
        TaskInfo taskInfo = Context.getTaskStorage().getTaskById(id);
        // 判断是否应该执行
        if (TaskUtil.shouldRemove(taskInfo)) {
          continue;
        }
        boolean shouldDo = TaskUtil.shouldDo(taskInfo.getNextTime(), time);
        if (shouldDo) {
          LOGGER.info("shouldDo. [taskId={}, taskSec={}, nowSec={}]",
              id, Context.getTimeWheelIndex(taskInfo.getNextTime()), Context.getTimeWheelIndex(time));
          try {
            ExecutePool.getInstance().submit(taskInfo, taskCaller);
          } catch (Exception e) {
            logException(taskInfo.toString(), e);
            taskInfo.setNextTime(CronUtil.nextExecuteTimeWithoutException(taskInfo));
            Context.getTaskStorage().updateNextTimeWithoutException(taskInfo.getId(), taskInfo.getNextTime());
            Context.getTaskProcessor().put(taskInfo);
          }

        } else {
          LOGGER.info("not do rePut. [taskId={}, now={}, nextTime={}",
              taskInfo.getId(), new Date(time), new Date(taskInfo.getNextTime()));
          Context.getTaskProcessor().put(taskInfo);
        }
      }
    }

  }

  private void sleepToNextSecond() {
    try {
      long sleep = 1000 - System.currentTimeMillis() % 1000;
      TimeUnit.MILLISECONDS.sleep(sleep);
    } catch (InterruptedException e) {
      logException(e.getMessage(), e);
    }
  }

}
