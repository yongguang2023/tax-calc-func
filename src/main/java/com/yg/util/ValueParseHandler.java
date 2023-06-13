package com.yg.util;


import java.util.function.Predicate;

public abstract class ValueParseHandler {
    public ValueParseHandler(Predicate<String> conditon) {
        this.conditon = conditon;
    }

    public Predicate<String> conditon= (s) -> false;

    public abstract String parseString(String formulaValue);
}
