package com.yg.util;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Map;

/**
 * @author wangx
 * @version 1.0
 * @date 2021/3/2 15:05
 */
public class TransmittableThreadLocalUtil {

  public static TransmittableThreadLocal<Map<String, Object>> paramsLocal = new TransmittableThreadLocal<>();

  public static TransmittableThreadLocal<Map<String, Object>> debugResultLocal = new TransmittableThreadLocal<>();

}
