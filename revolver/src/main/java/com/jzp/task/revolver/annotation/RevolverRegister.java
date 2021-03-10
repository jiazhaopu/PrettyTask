package com.jzp.task.revolver.annotation;


import com.jzp.task.revolver.constants.ScheduleType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RevolverRegister {

  Class<? extends Throwable>[] registerFor() default {Exception.class};

  String cron() default "";

  long nextTime() default 0;

  String handler();

  ScheduleType scheduleType();

  String content() default "";
  


}
