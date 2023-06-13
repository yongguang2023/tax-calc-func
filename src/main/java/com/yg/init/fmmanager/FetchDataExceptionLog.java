package com.yg.init.fmmanager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchDataExceptionLog {

  private String sheetName;

  private String location;

  private String atomicFormula;

  private String combinFormula;

  private Map<String, Object> params;

  public FetchDataExceptionLog(String atomicFormula, String combinFormula) {
    this.atomicFormula = atomicFormula;
    this.combinFormula = combinFormula;
  }


  public String convert2Msg() {
    String msg = "【sheet名称】：%s;\n【单元格位置】：%s;\n【组合公式】：%s;\n【原子公式】：%s;\n【取数参数】：%s";
    return String.format(msg, this.sheetName, this.location, this.combinFormula, this.atomicFormula,
        this.params);
  }
}
