package com.jzp.task.revolver.annotation;



import com.jzp.task.revolver.handler.ITaskHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 定时单次类型
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedRegister {


  /**
   * 哪种情况下会注册任务，默认抛出 Exception
   * 非必传
   */
  Class<? extends Throwable>[] registerFor() default {Exception.class};

  /**
   * 执行时间相对于限制，延迟多长时间
   */
  long delayTime();

  /**
   * 延迟时间单位，默认为秒
   * 非必传
   */
  TimeUnit delayUnit() default TimeUnit.SECONDS;

  /**
   * 执行任务的 handler
   */
  Class<? extends ITaskHandler> handler();

  /**
   * 自定义任务的名字
   * 非必传
   * 当线程池选择器为
   * 该值会参与执行线程池的选择
   */
  String name() default "";
}
