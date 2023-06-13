package com.yg.init.fmmanager;

import com.alibaba.ttl.TransmittableThreadLocal;


public class ThreadLocalHolder {

  public static TransmittableThreadLocal<String> GID = new TransmittableThreadLocal<>();

  public static TransmittableThreadLocal<Long> BOX_ID = new TransmittableThreadLocal<>();

  public static ThreadLocal<Boolean> LOG_ENABLE = new ThreadLocal<>();
}
