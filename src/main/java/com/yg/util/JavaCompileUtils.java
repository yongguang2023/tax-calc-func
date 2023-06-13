package com.yg.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/***
 * 公式工具类
 * @author zhengyang
 */
@Slf4j
public class JavaCompileUtils {

    /**
     * 默认的编译jar路径
     */
    private static final String DEFAULT_COMPILE_JAR_PATH = "/opt/tax_calc_kit/jars/";


    /**
     * 编译java公式返回字节码
     *
     * @return
     */
    public static byte[] compileJavaFormula(String content) {
        byte[] compiledByteArray = new byte[1];
        String jarPaths = CompileJavaProcess.getJarFiles(DEFAULT_COMPILE_JAR_PATH);
        if (StringUtils.isEmpty(content)) {
            throw new IllegalArgumentException("脚本不能为空");
        }
        //String javaFileContent = scriptText.replaceAll("package .*", "");

        Map<String, byte[]> compileByteMap = CompileJavaProcess.compile(content, jarPaths);
        // 判断是否编译成功
        if (compileByteMap != null) {
            compiledByteArray = compileByteMap.values().stream().findFirst().get();
        }
        return compiledByteArray;
    }
}
