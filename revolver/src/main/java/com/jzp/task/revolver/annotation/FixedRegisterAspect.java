package com.jzp.task.revolver.annotation;


import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.handler.ILogger;
import com.jzp.task.revolver.storage.FixedTask;
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
public class FixedRegisterAspect implements ILogger {

  @Pointcut("@annotation(com.jzp.task.revolver.annotation.FixedRegister)")
  private void doIntercept() {
  }

  @AfterThrowing(value = "doIntercept()", throwing = "ex")
  public void afterThrowing(JoinPoint point, Throwable ex) throws Exception {
    MethodSignature methodSignature = (MethodSignature) point.getSignature();
    Method method = methodSignature.getMethod();
    FixedRegister register = AnnotationUtils.getAnnotation(method, FixedRegister.class);
    if (register == null) {
      return;
    }
    catchAfterThrowing(register, ex);
  }

  private void catchAfterThrowing(FixedRegister register, Throwable e) throws Exception {
    if (e == null) {
      return;
    }
    if (register.registerFor().length > 0) {
      register(register);
    }
  }

  private void register(FixedRegister register) throws Exception {
    FixedTask taskInfo = new FixedTask();
    taskInfo.setContent(register.content());
    taskInfo.setHandler(register.handler());
    taskInfo.setName(register.name());
    long t = register.delayUnit().toMillis(register.delayTime());
    taskInfo.setExecuteTime(System.currentTimeMillis() + t);
    Context.getTaskClient().registerFixed(taskInfo);
  }


}