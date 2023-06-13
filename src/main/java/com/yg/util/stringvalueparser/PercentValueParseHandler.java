package com.yg.util.stringvalueparser;


import com.yg.exception.BizRuntimeException;
import lombok.Builder;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.function.Predicate;

/**
 * 百分号处理器
 */
public class PercentValueParseHandler extends ValueParseHandler {

    @Builder
    public PercentValueParseHandler(Predicate<String> conditon) {
        super(conditon);
    }

    @Override
    public String parseString(String formulaValue) {
        NumberFormat nf=NumberFormat.getPercentInstance();
        try {
            Number m=nf.parse(formulaValue);
            return m.toString();
        } catch (ParseException e) {
            throw new BizRuntimeException("");
        }
    }
}
