package com.yg.init.dsmanager;

import io.swagger.models.auth.In;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * jdbcTemplate池
 *
 * @author changpengye
 */
public class JdbcTemplatePool {

  private static Map<Integer, JdbcTemplate> jdbcTemplatePool = new HashMap<>();

  /**
   * 初始化jdbc连接池
   *
   * @param jdbcPool
   */
  public static void initPool(Map<Integer, JdbcTemplate> jdbcPool) {
    jdbcTemplatePool.putAll(jdbcPool);
  }

  /**
   * @param sourceId
   */
  public static void delete(Integer sourceId) {
    jdbcTemplatePool.remove(sourceId);
  }

  /**
   * 根据数据库的ID获取JdbcTemplate
   *
   * @param datasourceId
   * @return
   */
  public static JdbcTemplate getJdbcTemplate(Integer datasourceId) {
    return jdbcTemplatePool.get(datasourceId);
  }

}