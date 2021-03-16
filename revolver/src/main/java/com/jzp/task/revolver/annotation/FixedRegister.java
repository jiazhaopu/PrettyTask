package com.jzp.task.revolver.annotation;


import com.jzp.task.revolver.handler.ITaskHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedRegister {

  Class<? extends Throwable>[] registerFor() default {Exception.class};

  long delayTime();

  TimeUnit delayUnit() default TimeUnit.SECONDS;

  Class<? extends ITaskHandler> handler();

  String content() default "";

  String name() default "";
}
