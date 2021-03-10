package com.jzp.task.revolver;//package com.jzp;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.SynchronousQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class ExecutePool implements ILogger{
//
//  private static final ExecutePool pool = new ExecutePool();
//
//  TaskHandler taskHandler = new TaskHandler();
//  public static ExecutePool getInstance(){
//    return pool;
//  }
//
//  private ThreadPoolExecutor executor;
//
//  static AtomicInteger atomicInteger = new AtomicInteger(0);
//  private ExecutePool() {
//    executor = new ThreadPoolExecutor(Context.getConfig().getCorePoolSize(),
//        Context.getConfig().getCurrencyLevel(),
//        5L,
//        TimeUnit.MILLISECONDS,
////        new LinkedBlockingQueue<>(1),
//        new SynchronousQueue<>(),
//        new DefaultThreadFactory(),
//        new ThreadPoolExecutor.DiscardPolicy());
//  }
//
//  public void execute(final TaskInfo taskInfo, ITaskCallBack callable) {
////    System.out.println("submit taskInfoId="+taskInfo.getId()+",getActiveCount="+executor.getActiveCount());
//    executor.execute(()-> {
////        ITaskHandler handler = (ITaskHandler)ApplicationContextHelper.getBean(taskInfo.getHandler());
//        ITaskHandler handler = taskHandler;
//      try {
//        long t = System.currentTimeMillis();
//        System.out.println("start execute id="+taskInfo.getId()+" executorPoolSize="+executor.getActiveCount());
//        boolean res =  handler != null && handler.execute(taskInfo.getContent());
//        callable.call(taskInfo,res);
//        System.out.println("end execute id="+taskInfo.getId()+" executorSize="+executor.getActiveCount()+", cost="+(System.currentTimeMillis()-t));
//        atomicInteger.incrementAndGet();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    });
//  }
//
//  public void submit(final TaskInfo taskInfo, ITaskCallBack callable){
////    ForkJoinPool.commonPool().submit(new Runnable() {
////      @Override
////      public void run() {
////
////      }
////    });
//    CompletableFuture.runAsync(()-> {
////        ITaskHandler handler = (ITaskHandler)ApplicationContextHelper.getBean(taskInfo.getHandler());
//      ITaskHandler handler = taskHandler;
//      try {
//        long t = System.currentTimeMillis();
//        System.out.println("start execute id="+taskInfo.getId()+"");
//        boolean res =  handler != null && handler.execute(taskInfo.getContent());
//        callable.call(taskInfo,res);
//        System.out.println("end execute id="+taskInfo.getId()+", cost="+(System.currentTimeMillis()-t));
//        atomicInteger.incrementAndGet();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    });
//  }
//
//  public void submitForkJoinPool(final TaskInfo taskInfo, ITaskCallBack callable){
////    ForkJoinPool.commonPool().submit(new Runnable() {
////      @Override
////      public void run() {
////
////      }
////    });
//    ForkJoinPool.commonPool().execute(()-> {
////        ITaskHandler handler = (ITaskHandler)ApplicationContextHelper.getBean(taskInfo.getHandler());
//      ITaskHandler handler = taskHandler;
//      try {
//        long t = System.currentTimeMillis();
//        System.out.println("start execute id="+taskInfo.getId()+",ActiveThreadCount="
//            +ForkJoinPool.commonPool().getActiveThreadCount()+", getPoolSize="+ForkJoinPool.commonPool().getPoolSize()
//            +",getRunningThreadCount="+ForkJoinPool.commonPool().getRunningThreadCount()
//            +",getStealCount="+ForkJoinPool.commonPool().getStealCount()
//            +",getParallelism="+ForkJoinPool.commonPool().getParallelism());
//        boolean res =  handler != null && handler.execute(taskInfo.getContent());
//        callable.call(taskInfo,res);
//        System.out.println("end execute id="+taskInfo.getId()+", cost="+(System.currentTimeMillis()-t));
//        atomicInteger.incrementAndGet();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    });
//  }
//
//  public static void main(String[] args) throws InterruptedException {
//    ITaskCallBack taskCaller = new TaskExecuteCallBack();
//    long t = System.currentTimeMillis();
//    for (int i=0;i<100;i++){
//      TaskInfo taskInfo = new TaskInfo();
//      taskInfo.setId(i);
//      taskInfo.setContent(i+"");
//      taskInfo.setMaxExecuteTimes(1);
//      taskInfo.setScheduleType(ScheduleType.IMMEDIATELY.getCode());
//      taskInfo.setNextTime(System.currentTimeMillis());
////      ExecutePool.getInstance().execute(taskInfo,taskCaller);
//      ExecutePool.getInstance().submit(taskInfo,taskCaller);
//
////      Thread.sleep(1);
//    }
//    System.out.println("submit cost time="+(System.currentTimeMillis() - t));
//    while (ExecutePool.getInstance().executor.getActiveCount() != 0){
//
//    }
//    System.out.println("execute cost:"+(System.currentTimeMillis() - t)+", atomicInteger="+atomicInteger.get());
//
////
////    t = System.currentTimeMillis();
////    for (int i=0;i<100;i++){
////      TaskInfo taskInfo = new TaskInfo();
////      taskInfo.setId(i);
////      taskInfo.setContent(i+"");
////      taskInfo.setMaxExecuteTimes(1);
////      taskInfo.setScheduleType(ScheduleType.IMMEDIATELY.getCode());
////      taskInfo.setNextTime(System.currentTimeMillis());
////      ExecutePool.getInstance().submit(taskInfo,taskCaller);
//////      Thread.sleep(1);
////    }
////    System.out.println("forkJoinPoll submit cost time="+(System.currentTimeMillis() - t));
////    while (atomicInteger.get() != 100){
////
////    }
////    System.out.println("forkJoinPoll execute cost:"+(System.currentTimeMillis() - t)+", atomicInteger="+atomicInteger.get());
//
//
////    t = System.currentTimeMillis();
////    for (int i=0;i<100;i++){
////      TaskInfo taskInfo = new TaskInfo();
////      taskInfo.setId(i);
////      taskInfo.setContent(i+"");
////      taskInfo.setMaxExecuteTimes(1);
////      taskInfo.setScheduleType(ScheduleType.IMMEDIATELY.getCode());
////      taskInfo.setNextTime(System.currentTimeMillis());
////      ExecutePool.getInstance().submitForkJoinPool(taskInfo,taskCaller);
//////      Thread.sleep(1);
////    }
////    System.out.println("forkJoinPoll submit cost time="+(System.currentTimeMillis() - t));
////    while (atomicInteger.get() != 100){
////
////    }
////    System.out.println("forkJoinPoll execute cost:"+(System.currentTimeMillis() - t)+", atomicInteger="+atomicInteger.get());
//  }
//}
