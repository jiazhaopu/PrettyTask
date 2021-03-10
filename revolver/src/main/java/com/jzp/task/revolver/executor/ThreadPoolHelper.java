package com.jzp.task.revolver.executor;

import com.jzp.task.revolver.context.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolHelper {

  private static ThreadPoolExecutor failOverPool = new ThreadPoolExecutor(0, 1, 5L, TimeUnit.SECONDS
      , new LinkedBlockingQueue<>(1), new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "RevolverFailOverPool");
    }
  });

  public static ScheduledExecutorService schedulePool =
      Executors.newScheduledThreadPool(Context.getConfig().getScheduleThreadNum(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
          return new Thread(r, "RevolverScheduledThread");
        }
      });


  public static void submitToFailOverPool(Runnable runnable) {
    failOverPool.execute(runnable);
  }


}
