package com.yg.util.stringvalueparser;

import lombok.Builder;

import java.util.function.Predicate;

/**
 * 空字符串处理
 */
public class OriginStringValueParseHandler extends ValueParseHandler {
    @Override
    public String parseString(String formulaValue) {
        return formulaValue;
    }

    @Builder
    public OriginStringValueParseHandler(Predicate<String> conditon) {
        super(conditon);
    }
}
