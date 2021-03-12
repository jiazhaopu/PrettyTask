import com.jzp.task.revolver.TaskClient;
import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.handler.TaskHandler;
import com.jzp.task.revolver.storage.DBDataSource;
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
    for (int i = 0; i < 10; i++) {
      TaskInfo taskInfo = new TaskInfo();
      taskInfo.setHandler(TaskHandler.class.getName());
      taskInfo.setMaxExecuteTimes(10);
      taskInfo.setScheduleType(ScheduleType.CRON.getCode());
      taskInfo.setCron("20 * * * * ? ");
      taskInfo.setName("testTask");
      taskInfo.setContent(taskInfo.toString());
      taskInfo = taskClient.register(taskInfo);
      list.add(taskInfo.getId());

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
