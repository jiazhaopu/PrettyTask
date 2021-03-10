package com.jzp.task.revolver;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 业务方注入进来
 */
public class ApplicationContextHelper implements ApplicationContextAware {

  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  public static Object getBean(String beanName) {
    try {
      return context.getBean(beanName);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}