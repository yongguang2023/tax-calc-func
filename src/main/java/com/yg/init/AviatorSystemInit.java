package com.yg.init;

import com.yg.init.dsmanager.DataSourceManagerService;
import com.yg.init.fmmanager.FormulaManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author yechangpeng
 * @version 1.0.0
 * @ClassName AviatorSystemInit
 * @Description TODO
 * @createTime 2021年05月14日 16:55:00
 */
@Slf4j
@Service
public class AviatorSystemInit {

  @Resource
  private DataSourceManagerService dataSourceManagerService;

  @Resource
  private FormulaManagerService formulaManagerService;


  public void initAll() {
    log.info(
        "************************************************* aviator system init start *************************************************");
    dataSourceManagerService.initDataSource();
    formulaManagerService.createAviatorInstance();
    formulaManagerService.initAviatorFunction();
    log.info(
        "************************************************* aviator system init finish *************************************************");
  }
}
