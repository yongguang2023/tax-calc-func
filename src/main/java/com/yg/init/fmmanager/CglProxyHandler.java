package com.yg.init.fmmanager;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.yg.cache.FormulaExecutionCache;
import com.yg.exception.BizRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.util.DigestUtils;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * cglib代理类
 */
@Slf4j
public class CglProxyHandler implements MethodInterceptor {

  private static final String CALL = "call";

  /**
   * 不进行缓存的公式
   */
  private static final Set<String> EXCLUDES = Sets
      .newHashSet("IF", "ROUND", "RefTemplate", "SumF", "SumR");

  @Override
  public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) {
    try {
      //非call方法
      boolean proxy = method.getName().equals(CALL)
          //非排除的公式
          && !EXCLUDES.contains(((AbstractFunction) o).getName())
          //返回类型不为AviatorObject的自定义方法会重复进入代理方法，故只针对首次进入代理，再次进入则直接执行原方法
          && methodProxy.getSignature().getReturnType().getClassName()
          .equals(AviatorObject.class.getName());
      Object result = proxy ? this.execute(o, objects, methodProxy) : methodProxy.invokeSuper(o, objects);
      return result;
    } catch (Throwable e) {
      String formula = this.getFormula(o, objects);
      throw new BizRuntimeException(formula, e);
    }
  }


  /**
   * 执行方法
   *
   * @param o
   * @param objects
   * @param methodProxy
   * @return
   * @throws Throwable
   */
  private Object execute(Object o, Object[] objects, MethodProxy methodProxy) throws Throwable {
    final String gid = ThreadLocalHolder.GID.get();
    boolean cache = StringUtils.isNotBlank(gid); //&& (boolean) config.getValue(FORMULA_CACHE_ENABLE);

    Object result = null;
    StopWatch sw = new StopWatch();
    sw.start();
    String formula = this.getFormula(o, objects);
    cache = cache && StringUtils.isNotBlank(formula);
    try {
      result = cache ? this.runWithCache(formula, o, objects, methodProxy) :
          methodProxy.invokeSuper(o, objects);
    } catch (ExecutionException e) {
      throw e.getCause();
    } finally {
      sw.stop();
    }
    return result;
  }


  /**
   * 开启缓存
   *
   * @param o
   * @param objects
   * @param methodProxy
   * @return
   * @throws Throwable
   */
  private Object runWithCache(String formula, Object o, Object[] objects, MethodProxy methodProxy)
      throws Throwable {
    String key = this.getKey(formula);
    AtomicBoolean run = new AtomicBoolean(false);
    FutureTask<Object> task = this.getCache(key, v -> {
      run.set(true);
      return new FutureTask<>(() -> {
        try {
          return methodProxy.invokeSuper(o, objects);
        } catch (Throwable e) {
          throw (RuntimeException) e;
        }
      });
    });
    if (task == null) {
      return methodProxy.invokeSuper(o, objects);
    }
    if (run.get()) {
      task.run();
      log.info("公式缓存生成 ： {}", formula);
    } else {
      log.info("公式缓存生效 ： {}", formula);
    }
    return task.get();
  }

  /**
   * 从缓存获取公式执行结果
   *
   * @param key
   * @return
   */
  private FutureTask<Object> getCache(String key, Function<String, FutureTask<Object>> function) {
    try {
      String gid = ThreadLocalHolder.GID.get();
      return FormulaExecutionCache.getInstance().getCache(gid, key, function);
    } catch (Exception e) {
      log.error("get result from cache error,msg : {}", e.getMessage());
    }
    return null;
  }

  /**
   * 根据参数获取唯一值
   *
   * @param formula
   * @return
   */
  private String getKey(String formula) {
    try {
      if (StringUtils.isNotBlank(formula)) {
        return DigestUtils.md5DigestAsHex(formula.getBytes());
      }
    } catch (Exception e) {
      log.error("create key error:{}", e.getMessage());
    }
    return null;
  }

  /**
   * 获取完整公式
   *
   * @param o
   * @param objects
   * @return
   */
  private String getFormula(Object o, Object[] objects) {
    try {
      int len = objects.length;
      Object[] params = new Object[len - 1];
      String name = ((AbstractFunction) o).getName();
      Map<String, Object> env = (Map<String, Object>) objects[0];
      System.arraycopy(objects, 1, params, 0, len - 1);
      String s = JSON.toJSONString(this.convert(env, params));
      s = s.substring(1, s.length() - 1).replace("\"", "'");
      return name + "(" + s + ")";
    } catch (Exception e) {
      log.error("create complete formula error,msg : {}", e.getMessage());
    }
    return null;
  }

  /**
   * 将AviatorObject数组转为常规数组
   *
   * @param env
   * @param objects
   * @return
   */
  private Object[] convert(Map<String, Object> env, Object[] objects) {
    int len = objects.length;
    Object[] params = new Object[len];
    for (int i = 0; i < len; i++) {
      if (objects[i] instanceof AviatorObject) {
        AviatorObject value = (AviatorObject) objects[i];
        params[i] = value == null ? null : value.getValue(env);
      } else {
        AviatorObject[] values = (AviatorObject[]) objects[i];
        params[i] = values == null ? null : this.convert(env, values);
      }
    }
    return params;
  }
}
