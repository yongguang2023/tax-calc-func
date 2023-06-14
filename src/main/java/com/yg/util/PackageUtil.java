package com.yg.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PackageUtil {

    public static List<Class<?>> getClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace(".", "/");
        File dir = new File(Thread.currentThread().getContextClassLoader().getResource(path).getFile());
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    List<Class<?>> subClasses = getClasses(packageName + "." + fileName);
                    classes.addAll(subClasses);
                } else if (fileName.endsWith(".class")) {
                    String className = fileName.substring(0, fileName.length() - 6);
                    try {
                        classes.add(Class.forName(packageName + "." + className));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return classes;
    }
}
