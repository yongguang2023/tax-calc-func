package com.yg.util;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * id根据 systemId来区分
 * 编译公式类加载器
 */
public class RemoteClassLoader extends URLClassLoader {

    private byte[] bytes;

    public RemoteClassLoader(ClassLoader parent, byte[] bytes) {
        super(new URL[0], parent);
        this.bytes = bytes;
    }

    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        if (bytes != null) {
            return defineClass(null, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }
}
