package com.yg.util.stringvalueparser;

import com.yg.util.NumUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author yaowei
 * @Date 2021/7/8 16:46
 * Description:
 */
public class ParserUtils {

    /**
     * 百分号匹配正则
     */
    private static String REG = "(?:-?)(?:\\d{1,3}(?:[\\s,]\\d{3})+|\\d+)(?:\\.\\d{1,2})?(?:\\%)";

    /**
     * `1 计算结果处理
     *
     * @return
     */
    public static StringValueParser createValueParser() {
        StringValueParser parser = new StringValueParser();
        //父子引用 "%"好处理
        parser.register(PercentValueParseHandler.builder().conditon((s) -> s.matches(REG)).build());
        //常规数字处理
        parser.register(NumberValueParseHandler.builder().conditon(NumUtils::isNumeric).build());
        //父子引用"",或者字符串处理处理
        parser.register(OriginStringValueParseHandler.builder().conditon((s) ->
                StringUtils.isBlank(s) ||
                        !(NumUtils.isNumeric(s) && s.matches(REG))).build());
        return parser;
    }
}
