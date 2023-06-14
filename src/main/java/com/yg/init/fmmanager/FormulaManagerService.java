package com.yg.init.fmmanager;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Feature;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.googlecode.aviator.spring.SpringContextFunctionLoader;
import com.yg.config.ConfigDataSource;
import com.yg.entity.Datasource;
import com.yg.entity.Formula;
import com.yg.exception.BizRuntimeException;
import com.yg.init.dsmanager.JdbcTemplatePool;
import com.yg.service.DatasourceService;
import com.yg.service.FormulaService;
import com.yg.util.ApplicationUtil;
import com.yg.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FormulaManagerService  {

  @Resource
  private DatasourceService dataSourceService;

  @Resource
  private FormulaService formulaService;

  /**
   * 代理类
   */
  private static final CglProxyHandler CGLIB_PROXY = new CglProxyHandler();

  /**
   * 默认包路径
   */
  private static final String DEFAULT_PACKAGE = "com.yg.functions";

  public void initAviatorFunction() {
    log.info(
        "***************************************FormulaManagerServiceImpl initAviatorFunction start***************************************");
    List<Formula> formulas = formulaService.list();
    if (CollectionUtils.isEmpty(formulas)) {
      return;
    }

    formulas.forEach(formula -> this.createAviatorFunction(formula));

    //加载组合公式
    log.info(
        "***************************************FormulaManagerServiceImpl initAviatorFunction success***************************************");
  }


  /**
   * 根据系统id创建Aviator实例
   *
   */
  public void createAviatorInstance() {
    AviatorEvaluatorInstance instance = AviatorEvaluator.newInstance();
    // 小数相加丢失精度问题配置
    instance.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
    instance.enableFeature(Feature.LexicalScope);
    // aviator 版本升级兼容之前的版本
    instance.setOption(Options.FEATURE_SET, Feature.getCompatibleFeatures());
    // 不需要做编译优化
    instance.setOption(Options.OPTIMIZE_LEVEL, AviatorEvaluator.COMPILE);
    // 从spring容器获取公式
    instance.addFunctionLoader(new SpringContextFunctionLoader(ApplicationUtil.getApplicationContext()));
    AviatorCacheManager.getInstance().addAviatorInstance(instance);
    // 通过配置自动加入自定义function,这里不需要为每个实例初始化，走兜底方案
    List<Class<?>> classes = PackageUtil.getClasses(DEFAULT_PACKAGE);
    if (CollectionUtils.isNotEmpty(classes)) {
      classes.forEach(clazz -> {
        AviatorFunction function = this.findFunction(clazz.getName());
        if (function != null) {
          // 通过注解注入数据源
          this.configDataSourceByAnnotation(function);
          instance.addFunction(function);
        }
      });
    }
  }


  /**
   * 生成实例方法并放入缓存
   *
   * @param formula
   */
  public void createAviatorFunction(Formula formula) {
    try {
      log.info("FormulaManagerServiceImpl start loader function {} ",
              formula.getFuncName());
      AviatorCacheManager manager = AviatorCacheManager.getInstance();
      AviatorFunction aviatorFunction = this.createFunctionInstance(formula);
      // 通过注解注入数据源
      this.configDataSourceByAnnotation(aviatorFunction);
      //注入公式实例
      manager.getAviatorInstance().addFunction(aviatorFunction);
      log.info("FormulaManagerServiceImpl loader function {} success",
              formula.getFuncName());
    } catch (Exception e) {
      log.error("FormulaManagerServiceImpl loader function error,name : {},msg : {}",
              formula.getFuncName(), e);
    }
  }

  /**
   * 根据类名获取function实例
   *
   * @param className
   * @return
   */
  private AviatorFunction findFunction(String className) {
    try {
      if (!className.contains(".")) {
        className = DEFAULT_PACKAGE + className;
      }
      Class<? extends AviatorFunction> clz = (Class<? extends AviatorFunction>) Class
          .forName(className);
      try {
        return this.createProxy(clz);
      } catch (Exception e) {
      }
    } catch (Exception e) {
      log.error("load function by class name error,name : {},msg : {}", className, e.getMessage());
    }
    return null;
  }

  /**
   * 将公式脚本转为class文件
   *
   * @param formula
   * @return
   */
  private Class<?> convert2Class(Formula formula) {
    String name = formula.getFuncName();
    try (CustomClassLoader loader = new CustomClassLoader(getClass().getClassLoader())) {
      return loader.findClass(formula.getCompileContent());
    } catch (Throwable e) {
      log.error("create function class file error,name : {},msg : {}", name, e.getMessage());
      throw new BizRuntimeException("编译失败");
    }
  }


  /**
   * 生成代理实例
   *
   * @return
   */
  private AviatorFunction createFunctionInstance(Formula formula) {
    Class<?> clazz = convert2Class(formula);
    String name = formula.getName();
    if (!AbstractFunction.class.equals(clazz.getSuperclass())) {
      throw new BizRuntimeException(
          "This function does not inherit AviatorFunction.class,name : " + name);
    }
    return this.createProxy(clazz);
  }

  /**
   * 生成代理
   *
   * @param clazz
   * @return
   */
  private AviatorFunction createProxy(Class<?> clazz) {
    //生成代理类
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(clazz);
    enhancer.setCallback(CGLIB_PROXY);
    return (AviatorFunction) enhancer.create();
  }


  /**
   * 通过注解注入数据源
   *
   * @param function
   */
  private void configDataSourceByAnnotation(AviatorFunction function) {
    //这里取父类的class是因为目标类被代理了
    Class<?> clz = function.getClass().getSuperclass();
    Field[] fields = clz.getDeclaredFields();
    for (Field field : fields) {
      try {
        field.setAccessible(true);
        String value = null;
        boolean b = JdbcTemplate.class.equals(field.getType())
            && field.isAnnotationPresent(ConfigDataSource.class)
            && StringUtils.isNotBlank(value = field.getAnnotation(ConfigDataSource.class).value())
            && field.get(function) == null;
        if (b) {
          Datasource dataSource = dataSourceService.queryDataSourceByName(value);
          if (dataSource != null) {
            field.set(function, JdbcTemplatePool.getJdbcTemplate(dataSource.getId()));
          }
        }
      } catch (Exception e) {
        log.error("set data source by annotation error,msg : {}", e.getMessage());
      } finally {
        field.setAccessible(false);
      }
    }
  }
}
