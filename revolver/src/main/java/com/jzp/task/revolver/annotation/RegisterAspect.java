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

  /**
   * 执行业务方法之前
   */
  @Around("doIntercept()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Method method = methodSignature.getMethod();
    RevolverRegister register = AnnotationUtils.getAnnotation(method, RevolverRegister.class);
    if (register == null) {
      return null;
    }
    TaskInfo taskInfo = new TaskInfo();
    taskInfo.setContent(register.content());
    taskInfo.setCron(register.cron());
    taskInfo.setHandler(register.handler());
    Context.getTaskClient().register(taskInfo);

    return null;
  }


}