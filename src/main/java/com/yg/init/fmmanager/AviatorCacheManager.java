package com.yg.init.fmmanager;

import com.googlecode.aviator.AviatorEvaluatorInstance;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class AviatorCacheManager {

  private AviatorCacheManager() {
  }

  /**
   * 一个系统一个AviatorEvaluatorInstance
   */
  private AviatorEvaluatorInstance instance = null;

  private static AviatorCacheManager localGlobalData = new AviatorCacheManager();

  public static AviatorCacheManager getInstance() {
    return localGlobalData;
  }

  public void addAviatorInstance(AviatorEvaluatorInstance instance){
    this.instance = instance;
  }

  public AviatorEvaluatorInstance getAviatorInstance() {
    return this.instance;
  }

}
