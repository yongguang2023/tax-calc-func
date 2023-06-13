package com.yg.util;

public class IdGen {

    private static LongIdGenerator longIdGenerator = new LongIdGenerator(1L);

    public static String get() {
        return longIdGenerator.generate().toString();
    }
}
