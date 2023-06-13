package com.yg.util.stringvalueparser;

import lombok.Builder;

import java.util.function.Predicate;

/**
 * 正常数字处理
 */
public class NumberValueParseHandler extends ValueParseHandler {
    @Builder
    public NumberValueParseHandler(Predicate<String> conditon) {
        super(conditon);
    }

    @Override
    public String parseString(String formulaValue) {
        return  formulaValue;
    }

}
