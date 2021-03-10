import com.jzp.task.revolver.Config;
import com.jzp.task.revolver.Context;
import com.jzp.task.revolver.DBDataSource;
import com.jzp.task.revolver.IPUtils;
import com.jzp.task.revolver.ScheduleType;
import com.jzp.task.revolver.TaskClient;
import com.jzp.task.revolver.model.TaskInfo;
import com.jzp.task.revolver.register.RegisterCenter;
import com.jzp.task.revolver.register.ZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class ZkTest {

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

    TaskClient taskClient = new TaskClient(Collections.singletonList(source),config);
    taskClient.init();
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

    for (int i=0;i<20;i++){

      TaskInfo taskInfo =new TaskInfo();
      taskInfo.setContent(i+"");
      taskInfo.setHandler(""+i);
      taskInfo.setHost(IPUtils.getHostAddress());
      taskInfo.setMaxExecuteTimes(10);
      taskInfo.setScheduleType(ScheduleType.RETRY.getCode());
      taskClient.register(taskInfo);
    }

  }
}
