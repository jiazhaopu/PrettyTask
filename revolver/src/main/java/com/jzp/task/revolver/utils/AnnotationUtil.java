package com.jzp.task.revolver.utils;

import org.aspectj.lang.JoinPoint;

public class AnnotationUtil {

  /**
   * 获取方法注解上参数值的序列化结果
   */
  public static String getParaStringValue(JoinPoint point) {
    Object[] objects = point.getArgs();
    if (objects.length > 1) {
      return null;
    }
    if (objects.length > 0) {
      return JsonUtil.writeValueAsString(objects[0]);
    }
    return null;
  }
}
