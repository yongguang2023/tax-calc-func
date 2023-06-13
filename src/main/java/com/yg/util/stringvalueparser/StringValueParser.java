package com.yg.util.stringvalueparser;

import com.google.common.collect.Lists;
import com.yg.exception.BizRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * 公式解析处理
 */
public class StringValueParser {

    private List<ValueParseHandler> valueParseHandlers = Lists.newArrayList();

    public String doParse(String value) {
        if(StringUtils.isBlank(value)){
            return value;
        }

        if (CollectionUtils.isEmpty(valueParseHandlers)) {
            throw new BizRuntimeException("至少需要一个处理器解析公式");
        }

        Optional<ValueParseHandler> valueParseHandler =
                valueParseHandlers.stream().filter(hander -> hander.conditon.test(value)).findFirst();
        return valueParseHandler.orElseThrow(() -> new BizRuntimeException("没有找到匹配器,至少需要一个处理器解析公式")).parseString(value);
    }

    /**
     * 注册处理器
     * @param valueParseHandler
     */
    public void register(ValueParseHandler valueParseHandler) {
        valueParseHandlers.add(valueParseHandler);
    }
}
