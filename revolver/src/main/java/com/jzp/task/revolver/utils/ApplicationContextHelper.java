package com.jzp.task.revolver.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 业务方注入进来
 */
@Component
@Lazy(false)
public class ApplicationContextHelper implements ApplicationContextAware {

  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  public static Object getBeanByClassName(String beanName) {

    try {
      Class clz = Class.forName(beanName);
      return getBean(clz);
    } catch (Exception e) {
    }
    return null;
  }

  public static Object getBean(String beanName) {

    try {
      return context.getBean(beanName);
    } catch (Exception e) {
    }
    return null;
  }


  public static <T> T getBean(String beanName, Class<T> clazz) {
    try {
      return context.getBean(beanName, clazz);
    } catch (Exception e) {
    }
    return null;
  }


  public static <T> T getBean(Class<T> clazz) {
    try {
      return context.getBean(clazz);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


}