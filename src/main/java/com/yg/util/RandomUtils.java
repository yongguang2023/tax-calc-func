package com.yg.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class RandomUtils {

  private static SimpleDateFormat fmat = new SimpleDateFormat("yyyyMMddHHmmss");

  private RandomUtils() {
  }

  /**
   * 获取32位uuid
   */
  public static String getUuid() {
    return UUID.randomUUID().toString().replace("-", "").toUpperCase();
  }

  /**
   * 获取指定长度的随机数
   */
  public static Long getRandom(int length) {
    length = length > 0 ? length : -length;
    return (long) (Math.pow(10.0, length) * Math.random());
  }

  /**
   * 获取随机文件名yyyyMMddHHmmss+4位随机数
   */
  public static String getFileName() {
    return fmat.format(new Date()) + getRandom(4);
  }
}
