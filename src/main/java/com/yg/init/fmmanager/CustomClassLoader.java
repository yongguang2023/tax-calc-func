package com.yg.init.fmmanager;

import java.net.URL;
import java.net.URLClassLoader;


public class CustomClassLoader extends URLClassLoader {

  public CustomClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
  }

  public Class<?> findClass(byte[] bytes) {
    return super.defineClass(null, bytes, 0, bytes.length);
  }
}
