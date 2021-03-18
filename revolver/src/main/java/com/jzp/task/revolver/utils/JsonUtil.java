package com.jzp.task.revolver.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonUtil {

  static Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

  public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

  public static final ObjectMapper CUSTOMER_OBJECT_MAPPER = new ObjectMapper();

  static {
    // 容忍json中出现未知的列
    DEFAULT_OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // 兼容java中的驼峰的字段名命名
    DEFAULT_OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    // 容忍json中出现未知的列
    CUSTOMER_OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // 兼容java中的驼峰的字段名命名
    CUSTOMER_OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    // 忽略 null 值
    CUSTOMER_OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
  }

  /**
   * 将对应的规则转换为不同的类
   * 这里的封装主要是处理 null 和异常情况
   */
  public static <T> T readValue(String content, Class<T> tClass) {
    try {
      if (null == content) {
        return null;
      }
      return DEFAULT_OBJECT_MAPPER.readValue(content, tClass);
    } catch (Exception e) {
      LOGGER.error("failed to parse content to specified class. [content={}, class={}]", content, tClass.getName(), e);
      throw new RuntimeException("failed to parse content.", e);
    }
  }

  /**
   * JSON --> List<Object>
   */
  public static <T> T readValue(String content, TypeReference<T> valueTypeRef) {
    try {
      if (null == content) {
        return null;
      }
      return DEFAULT_OBJECT_MAPPER.readValue(content, valueTypeRef);
    } catch (Exception e) {
      LOGGER.error("failed to parse content to list. [type={}]", valueTypeRef.getType(), e);
      throw new RuntimeException("failed to parse content.", e);
    }
  }

  /**
   * 将对象转成字符串
   * 这里的封装主要是处理 null 和异常情况
   */
  public static String writeValueAsString(Object object) {
    try {
      if (null == object) {
        return null;
      }
      return DEFAULT_OBJECT_MAPPER.writeValueAsString(object);
    } catch (Exception e) {
      LOGGER.error("failed to write value as string. [class={}]", object.toString(), e);
      throw new RuntimeException("failed to write value as string.", e);
    }
  }

  /**
   * 将对象转成字符串 忽略空值
   */
  public static String writeValueAsStringIgnoreNull(Object object) {
    try {
      if (null == object) {
        return null;
      }
      return CUSTOMER_OBJECT_MAPPER.writeValueAsString(object);
    } catch (Exception e) {
      LOGGER.error("failed to write value as string. [class={}]", object.toString(), e);
      throw new RuntimeException("failed to write value as string.", e);
    }
  }

  public static String getJsonNodePropAsText(JsonNode jsonNode, String propName) {
    JsonNode propNode = jsonNode.get(propName);
    return propNode == null ? null : propNode.asText();
  }
}
