package com.jzp.task.revolver.annotation;


import com.jzp.task.revolver.handler.ITaskHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RetryRegister {

  Class<? extends Throwable>[] registerFor() default {Exception.class};

  String cron() default "";

  Class<? extends ITaskHandler> handler();

  String content() default "";

  // 0 表示不限次数
  int maxExecuteTimes();

  String name() default "";

}
