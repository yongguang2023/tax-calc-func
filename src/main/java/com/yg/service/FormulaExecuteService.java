package com.yg.service;

import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.exception.FunctionNotFoundException;
import com.yg.exception.BizRuntimeException;
import com.yg.exception.BusinessException;
import com.yg.init.fmmanager.AviatorCacheManager;
import com.yg.init.fmmanager.FetchDataExceptionLog;
import com.yg.util.NumUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class FormulaExecuteService {

  @Value("${formula.result.exclude.reg:^((ROUND)|(RefTemplate)|(UDEF_REMOVEPOINT)|(KXJS)|(CJ_SFCJ) |(JJKC_SFJJKC)|(SFCFL)|(QUERY_ZTDMBYQYID)|(SumR)).+}")
  private String excludes;

  public Object executeFormula(String formula, Map<String, Object> params) {
    AviatorEvaluatorInstance instance = AviatorCacheManager.getInstance().getAviatorInstance();
    if (instance == null) {
      throw new BizRuntimeException("aviator instance has not init");
    }
    try {
      Expression expression = instance.compile(formula, false);
      Object value = expression.execute(new HashMap<>(params));
      return this.formatNumber(formula, value);
    } catch (BizRuntimeException | FunctionNotFoundException e) {
      String message = e.getMessage();
      FetchDataExceptionLog exlog = new FetchDataExceptionLog(message, formula);
      throw new BusinessException(e, exlog);
    } catch (Exception e) {
      FetchDataExceptionLog exlog = new FetchDataExceptionLog(formula, formula);
      throw new BusinessException(e, exlog);
    }
  }


  /**
   * @param formula
   * @param value
   * @return
   */
  private Object formatNumber(String formula, Object value) {
    String s = value.toString();
    boolean isNumber = NumUtils.isNumeric(s);
    if (!isNumber || StringUtils.isBlank(formula) || value instanceof String) {
      return value;
    }
    if (!formula.matches(excludes)) {
      s = NumUtils.formatDecimal(s);
    } else if (value instanceof BigDecimal) {
      s = ((BigDecimal) value).toPlainString();
    }
    return s;
  }
}
