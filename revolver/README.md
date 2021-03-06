# Revolver 左轮手枪
## Introduction
Revolver 是一款轻量级、及时产生的分布式任务调度工具

场景举例：由于某个原因，实时产生了一个任务，可能分为以下几种，注册任务按规则调度
- 单次任务。某个时间发一封邮件
- 重试机制。例如分布式事务数据一致性
- 重复调度任务

Revolver 像左轮一样轻量，使用者不断向弹药库或者转轮输送弹药，同时有一个线程定时从弹药库加入转轮，Revolver 每隔一秒从转轮取出子弹。
## Features
- 1.及时产生。如用户的某个操作引发了某个延时任务
- 1. api 式注册
- 2. AOP 注解式注册
- 3. 支持启动暂停
- 2.支持的任务类型：
- 1. FIXED_TIME。只能执行一次，需要指定执行时间，或者 CRON
- 2. RETRY。需要指定重试次数，如果指定 CRON 则按照规则重试，否则随机间隔 1～5 秒调度
- 3. CRON。按照 CRON 调度，可不指定次数上限

- 4.动态分片。自产自销，哪台机器产生任务哪台执行，支持故障转移重分片
- 5.持久化。mysql 持久化任务
- 6.注册中心。所有执行任务的节点都注册到zookeeper
- 7.故障转移。节点故障时，由zookeeper通知其他可用节点分摊任务
- 8.秒级调度。支持 CRON 或 指定调度时间的秒级调度

- 9.秒级别并发度计算：

平均并发度 = 任务调度线程池数(executePoolsNum) * 核心线程数(corePoolSize) / (1s / one task cost time)
- 10.执行线程池隔离。减少任务间影响

- 11.任务调度线程池选择策略。
- 1. hash(推荐)。根据任务名hash到固定线程池，特点是任务所在线程池是固定的，可以控制任务的并发度，算法简单。一般情况下任务分布比较均匀，极端情况线程池利用率不均
- 2. 轮询。下一个线程池
- 3. 随机。
- 4. 平衡(默认)(推荐)。当任务没有进入过线程池时，轮询选择下一个线程池，一旦选择了线程池就在固定线程池执行。是对hash策略的优化，可以控制任务的并发度
- 5. 低负载(推荐)。选择当前负载最低的线程池，优点效率高，缺点执行线程池不确定，多线程并发不好控制

## Use

```$xslt
// 启动扫描
@SpringBootApplication
@EnableAspectJAutoProxy
@Import({RevolverSpringConfiguration.class})
public class WebServer {
  public static void main(String[] args) {
    SpringApplication.run(WebServer.class, args);
  }
}

```

```$xslt

import com.jzp.task.revolver.annotation.RevolverRegister;
import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.context.Config;
import com.jzp.task.revolver.storage.DBDataSource;
import com.jzp.task.revolver.storage.TaskInfo;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Lazy(false)
public class TaskClient {

    // 初始化连接
  private com.jzp.task.revolver.TaskClient taskClient;
  public TaskClient() throws Exception {
    init();
  }
  private synchronized void init() throws Exception {
    if (taskClient == null) {
      DBDataSource source = new DBDataSource(url, user, password);
      source.setMaster(true);
      dbDataSources.add(source);
      Config config = new Config();
      config.setZookeeperConnect("zk_url:port");
      config.setProduct("myProduct");
      config.setModule("myService);
      config.setZookeeperRootPath("/test/");
      taskClient = new com.jzp.task.revolver.TaskClient(dbDataSources, config);
    }
  }

  public TaskInfo register(CronTask cronTask) {
    try {

      return taskClient.registerCron(cronTask);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;

  }

  @CronRegister(cron = "20 * * * * ? ", handler = TaskHandler.class, maxExecuteTime = 10)
  public void cronRegister() {
    
  }

  @FixedRegister(handler = TaskHandler.class, delayTime = 5)
  public void fixedRegister() {
    
  }

  @RetryRegister(handler = TaskHandler.class, maxExecuteTimes = 10)
  public void retryRegister() {
     
  }

}

```

```$xslt
实现接口
import com.jzp.task.revolver.handler.ITaskHandler;
import org.springframework.stereotype.Component;

@Component("taskHandler")
public class TaskHandler implements ITaskHandler {
  @Override
  public ResultEnum execute(String s) throws Exception {
    do something
    return ResultEnum.FINISH;
  }
}

```