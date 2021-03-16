import com.jzp.task.revolver.TaskClient;
import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.handler.TaskHandler;
import com.jzp.task.revolver.storage.CronTask;
import com.jzp.task.revolver.storage.DBDataSource;
import com.jzp.task.revolver.storage.FixedTask;
import com.jzp.task.revolver.storage.RetryTask;
import com.jzp.task.revolver.storage.TaskInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Test {


  public static void main(String[] args) throws Exception {
    DBDataSource source = new DBDataSource();
    source.setMaster(true);
    source.setUrl("jdbc:mysql://localhost:3306/pio");
    source.setUsername("root");
    source.setPassword("root");

    Config config = new Config();
    config.setZookeeperConnect("zk_host:2181");
    config.setZookeeperRootPath("/sensors_analytics");
    config.setProduct("sfn");
    config.setModule("web");

    TaskClient taskClient = new TaskClient(Collections.singletonList(source), config);

    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      CronTask taskInfo = new CronTask();
      taskInfo.setHandler(TaskHandler.class);
      taskInfo.setMaxExecuteTimes(10);
      taskInfo.setCron("20 * * * * ? ");
      taskInfo.setName("testTask");
      taskInfo.setContent(taskInfo.toString());
      TaskInfo taskInfo1 = taskClient.registerCron(taskInfo);
      list.add(taskInfo1.getId());
    }

    for (int i = 0; i < 100; i++) {
      FixedTask taskInfo = new FixedTask();
      taskInfo.setHandler(TaskHandler.class);
      taskInfo.setExecuteTime(System.currentTimeMillis() + 1000);
      taskInfo.setName("testTask");
      taskInfo.setContent(taskInfo.toString());
      TaskInfo taskInfo1 = taskClient.registerFixed(taskInfo);
      list.add(taskInfo1.getId());
    }

    for (int i = 0; i < 100; i++) {
      RetryTask taskInfo = new RetryTask();
      taskInfo.setHandler(TaskHandler.class);
      taskInfo.setCron("20 * * * * ? ");
      taskInfo.setMaxExecuteTimes(10);
      taskInfo.setName("testTask");
      taskInfo.setContent(taskInfo.toString());
      TaskInfo taskInfo1 = taskClient.registerRetry(taskInfo);
      list.add(taskInfo1.getId());
    }

//
//    Thread.sleep(1000 * 30);
//    for (Integer integer : list) {
//      taskClient.suspend(integer);
//    }
//
//    Thread.sleep(1000 * 30);
//    for (Integer integer : list) {
//      taskClient.start(integer);
//    }


  }

}
