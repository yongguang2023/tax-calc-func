package com.yg.cache;

import java.util.concurrent.FutureTask;

/**
 * 公式执行结果缓存
 */
public class FormulaExecutionCache extends AbstractCache<FutureTask<Object>> {

  private FormulaExecutionCache() {
  }

  private static volatile FormulaExecutionCache instance = null;

  public static FormulaExecutionCache getInstance() {
    if (instance == null) {
      synchronized (FormulaExecutionCache.class) {
        instance = new FormulaExecutionCache();
      }
    }
    return instance;
  }
}

