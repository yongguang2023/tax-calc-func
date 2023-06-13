package com.yg.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class AbstractCache<T> {

  /**
   * 缓存
   */
  private final Map<String, Map<String, T>> cache = new ConcurrentHashMap<>();

  /**
   * 初始化缓存容器
   *
   * @param gid
   */
  public void initCache(String gid) {
    if (StringUtils.isNotBlank(gid)) {
      cache.computeIfAbsent(gid, v -> new ConcurrentHashMap<>());
    }
  }

  /**
   * @param gid
   * @param key
   * @param value
   */
  public void setCache(String gid, String key, T value) {
    try {
      if (StringUtils.isNoneBlank(gid, key) && cache.containsKey(gid) && value != null) {
        cache.get(gid).put(key, value);
      }
    } catch (Exception e) {
      log.error("设置缓存失败,gid : {},msg : {}", gid, e.getMessage());
    }
  }

  /**
   * @param gid
   * @param values
   */
  public void setCache(String gid, Map<String, T> values) {
    try {
      if (StringUtils.isNotBlank(gid) && cache.containsKey(gid) && values != null) {
        cache.get(gid).putAll(values);
      }
    } catch (Exception e) {
      log.error("设置缓存失败,gid : {},msg : {}", gid, e.getMessage());
    }
  }

  /**
   * 获取缓存
   *
   * @param gid
   * @param key
   * @return
   */
  public T getCache(String gid, String key) {
    try {
      if (StringUtils.isNoneBlank(gid, key) && cache.containsKey(gid)) {
        return cache.get(gid).get(key);
      }
    } catch (Exception e) {
      log.error("获取缓存失败,gid : {},msg : {}", gid, e.getMessage());
    }
    return null;
  }

  /**
   * @param gid
   * @param key
   * @param function
   * @return
   */
  public T getCache(String gid, String key, Function<String, T> function) {
    try {
      if (StringUtils.isNoneBlank(gid, key) && cache.containsKey(gid)) {
        return cache.get(gid).computeIfAbsent(key, function);
      }
    } catch (Exception e) {
      log.error("获取缓存失败,gid : {},msg : {}", gid, e.getMessage());
    }
    return null;
  }

  /**
   * 清除单次取数缓存
   *
   * @param gid
   */
  public void clearCache(String gid) {
    if (StringUtils.isNotBlank(gid)) {
      cache.remove(gid);
    }
  }
}
