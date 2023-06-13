package com.yg.functions.base;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("IF")
public class IF extends AbstractFunction {


    @Override
    public String getName() {
        return "IF";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg, AviatorObject arg1, AviatorObject arg2) {
        return arg.booleanValue(env) ? arg1 : arg2;
    }
}
