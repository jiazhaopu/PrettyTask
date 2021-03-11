import com.jzp.task.revolver.TaskClient;
import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.storage.DBDataSource;
import com.jzp.task.revolver.storage.TaskInfo;
import com.jzp.task.revolver.utils.IPUtils;

import java.util.Collections;

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
//
//    ZookeeperClient client = Context.getZookeeperClient();
//    RegisterCenter registerCenter = new RegisterCenter(client);
//
//    client.createPath(registerCenter.getRevolverPath());
//    client.createPath(registerCenter.getProductPath());
//    client.createPath(registerCenter.getModulePath());
//
//
////    String dataPath = registerCenter.getRegisterPath(Context.getConfig().getProduct(),Context.getConfig().getModule());
//
////    String rootPath = client.getZookeeperRootPath() + path;
////    client.createPath(dataPath);
////    client.createEphemeralPathAndSetData(rootPath+"/"+UUID.randomUUID().toString(),"{\"ip\":\"127\"}");
////    client.createEphemeralPathAndSetData(rootPath+"/"+UUID.randomUUID().toString(), "123");
//
//    registerCenter.setDataForEphe(IPUtils.getHostAddress()+"-1");
//    registerCenter.setDataForEphe(IPUtils.getHostAddress()+"-2");
//    registerCenter.setDataForEphe(IPUtils.getHostAddress()+"-3");
//
//    client.watch(registerCenter.getModulePath());

    for (int i = 0; i < 1000; i++) {

      TaskInfo taskInfo = new TaskInfo();
      taskInfo.setContent(i + "");
      taskInfo.setHandler("" + i % 5);
      taskInfo.setHost(IPUtils.getHostAddress());
      taskInfo.setMaxExecuteTimes(10);
      taskInfo.setScheduleType(ScheduleType.RETRY.getCode());
      taskInfo = taskClient.register(taskInfo);
      taskClient.suspend(taskInfo.getId());
      taskClient.start(taskInfo.getId());
      Thread.sleep(1000);
    }

  }
}
