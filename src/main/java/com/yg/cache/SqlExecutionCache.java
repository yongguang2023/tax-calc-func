package com.yg.cache;

import java.util.concurrent.FutureTask;

/**
 * 公式执行结果缓存
 */
public class SqlExecutionCache extends AbstractCache<FutureTask<Object>> {

  private SqlExecutionCache() {
  }

  private static volatile SqlExecutionCache instance = null;

  public static SqlExecutionCache getInstance() {
    if (instance == null) {
      synchronized (SqlExecutionCache.class) {
        instance = new SqlExecutionCache();
      }
    }
    return instance;
  }
}

