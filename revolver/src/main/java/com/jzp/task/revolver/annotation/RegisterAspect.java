package com.jzp.task.revolver.annotation;


import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.storage.TaskInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
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
public class RegisterAspect {

  @Pointcut("@annotation(com.jzp.task.revolver.annotation.RevolverRegister)")
  private void doIntercept() {
  }

  @Around("doIntercept()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Method method = methodSignature.getMethod();
    RevolverRegister register = AnnotationUtils.getAnnotation(method, RevolverRegister.class);
    if (register == null) {
      return null;
    }
    try {
      return joinPoint.proceed();
    } catch (Throwable e) {
      catchAfterThrowing(register, e);
      throw e;
    }
  }

  private void catchAfterThrowing(RevolverRegister register, Throwable e) throws Exception {
    if (e == null) {
      return;
    }
    if (register.registerFor().length > 0) {
      register(register);
    }
  }

  private void register(RevolverRegister register) throws Exception {
    TaskInfo taskInfo = new TaskInfo();
    taskInfo.setContent(register.content());
    taskInfo.setCron(register.cron());
    taskInfo.setHandler(register.handler());
    taskInfo.setNextTime(register.nextTime());
    taskInfo.setMaxExecuteTimes(register.maxExecuteTimes());
    taskInfo.setScheduleType(register.scheduleType().getCode());
    Context.getTaskClient().register(taskInfo);
  }


}