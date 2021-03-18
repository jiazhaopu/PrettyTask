package com.jzp.task.revolver.annotation;


import com.jzp.task.revolver.handler.ITaskHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重试类型
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RetryRegister {

  /**
   * 哪种情况下会注册任务，默认抛出 Exception
   */
  Class<? extends Throwable>[] registerFor() default {Exception.class};

  /**
   * 任务的执行时间规则
   * 非必传
   */
  String cron() default "";

  /**
   * 执行任务的 handler
   */
  Class<? extends ITaskHandler> handler();

  /**
   * 最多执行次数，默认0，不限制
   */
  int maxExecuteTimes();

  /**
   * 自定义任务的名字
   * 非必传
   * 当线程池选择器为
   * 该值会参与执行线程池的选择
   */
  String name() default "";

}
