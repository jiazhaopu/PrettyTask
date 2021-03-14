package com.jzp.task.revolver.handler;

import com.jzp.task.revolver.utils.ApplicationContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerContainer {

  static Logger LOGGER = LoggerFactory.getLogger(HandlerContainer.class);

  private static final Map<String, Object> nameContainer = new ConcurrentHashMap<>();

  private static final Map<String, Object> nameClassContainer = new ConcurrentHashMap<>();

  public static <T> T getBean(String name) {
    Object o = ApplicationContextHelper.getBean(name);
    if (o == null) {
      try {
        Class clz = Class.forName(name);
        o = ApplicationContextHelper.getBean(clz);
      } catch (Exception e) {
        e.printStackTrace();

      }
      if (o == null) {
        return get(name);
      }
    }
    return (T) o;
  }

  public static <T> T getBean(String name, Class<T> clazz) {
    Object o = ApplicationContextHelper.getBean(name, clazz);
    if (o == null) {
      return get(name, clazz);
    }
    return (T) o;
  }

  private static <T> T get(String name) {
    try {
      T handler = (T) nameContainer.get(name);
      if (handler == null) {
        Class clz = Class.forName(name);
        handler = (T) clz.newInstance();
        nameContainer.put(name, handler);
      }
      return handler;
    } catch (Exception e) {
      LOGGER.error("failed to get handler. [name='{}']", name, e);
    }
    return null;
  }

  private static String key(String name, String targetName) {
    return name + "_" + targetName;
  }

  private static <T> T get(String name, Class<T> clazz) {
    try {
      String key = key(name, clazz.getName());
      T handler = (T) nameClassContainer.get(key);
      if (handler == null) {
        Class clz = Class.forName(name);
        handler = (T) clz.newInstance();
        nameClassContainer.put(key, handler);
      }
      return handler;
    } catch (Exception e) {
      LOGGER.error("failed to get handler. [name='{}', clazz='{}']", name, clazz.getName(), e);
    }
    return null;
  }

}
