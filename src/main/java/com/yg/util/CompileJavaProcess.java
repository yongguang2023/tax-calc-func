package com.yg.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: java脚本编译工具
 * @Author: jeffery.gao
 * @CreateDate: 2018/12/12 9:20
 * @UpdateDate: 2018/12/12 9:20
 * @Version: 1.0
 * @UpdateUser:
 * @UpdateRemark:
 */
@Slf4j
public class CompileJavaProcess {
    /**
     * 寻找java类名正则
     */
    private static final String FIND_CLASS_NAME_REG = "public\\s+class\\s+(\\w+)";

    /**
     * 编译java源码
     *
     * @param javaSrc
     * @return
     */
    public static Map<String, byte[]> compile(String javaSrc,String classPathJars) {
        Pattern pattern = Pattern.compile(FIND_CLASS_NAME_REG);
        Matcher matcher = pattern.matcher(javaSrc);
        if (matcher.find()) {
            return compile(matcher.group(1) + ".java", javaSrc, classPathJars);
        }
        return null;
    }

    /**
     * 指定java类名编译java源码
     *
     * @param javaName
     * @param javaSrc
     * @return
     */
    public static Map<String, byte[]> compile(String javaName, String javaSrc,String classPathJars) {

        //检查当前jar包的最后时间，检测创建目录的时间
        //如果不一致，则解压相应的jar包到指定目录
        //从指定的目录加载jar包
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
        MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager);
        JavaFileObject javaFileObject = MemoryJavaFileManager.makeStringSource(javaName, javaSrc);

        List<String> options = new ArrayList<>();
        if(StringUtils.isNotBlank(classPathJars)){
            options = Arrays.asList("-encoding", "UTF-8","-classpath", classPathJars);
        }else{
            options = Arrays.asList("-encoding", "UTF-8");
        }
        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, options, null, Arrays.asList(javaFileObject));
        if (task.call()) {
            return manager.getClassBytes();
        }
        return null;
    }

    /**
     * 查找该目录下的所有的jar文件
     *
     * @param jarPath
     */
    public static String getJarFiles(String jarPath) {
        File sourceFile = new File(jarPath);
        StringBuilder jars=new StringBuilder();
        // 文件或者目录必须存在
        if (sourceFile.exists()) {
            // 若file对象为目录
            if (sourceFile.isDirectory()) {
                // 得到该目录下以.java结尾的文件或者目录
                File[] childrenFiles = sourceFile.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.isDirectory()) {
                            return true;
                        } else {
                            String name = pathname.getName();
                            if (name.endsWith(".jar")) {
                                if(jars.length()>0){
                                    if(System.getProperties().getProperty("os.name").toLowerCase().indexOf("windows") > -1)
                                        jars.append(";");
                                    else
                                        jars.append(":");
                                }
                                jars.append(pathname.getPath());
                                return true;
                            }
                            return false;
                        }
                    }
                });
            }
        }
        return jars.toString();
    }


}
