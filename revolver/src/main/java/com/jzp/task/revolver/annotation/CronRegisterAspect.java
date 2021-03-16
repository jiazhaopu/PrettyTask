package com.jzp.task.revolver.annotation;


import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.log.ILogger;
import com.jzp.task.revolver.storage.CronTask;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Revolver 任务注册
 *
 * @author jiazhaopu
 * @since 2021-03-11
 */
@Component
@Aspect
public class CronRegisterAspect implements ILogger {

  @Pointcut("@annotation(com.jzp.task.revolver.annotation.CronRegister)")
  private void doIntercept() {
  }

  @AfterThrowing(value = "doIntercept()", throwing = "ex")
  public void afterThrowing(JoinPoint point, Throwable ex) throws Exception {
    MethodSignature methodSignature = (MethodSignature) point.getSignature();
    Method method = methodSignature.getMethod();
    CronRegister register = AnnotationUtils.getAnnotation(method, CronRegister.class);
    if (register == null) {
      return;
    }
    catchAfterThrowing(register, ex);
  }

  private void catchAfterThrowing(CronRegister register, Throwable e) throws Exception {
    if (e == null) {
      return;
    }
    if (register.registerFor().length > 0) {
      register(register);
    }
  }

  private void register(CronRegister register) throws Exception {
    CronTask taskInfo = new CronTask();
    taskInfo.setContent(register.content());
    taskInfo.setCron(register.cron());
    taskInfo.setHandler(register.handler());
    taskInfo.setName(register.name());
    taskInfo.setMaxExecuteTimes(register.maxExecuteTime());
    Context.getTaskClient().registerCron(taskInfo);
  }


}