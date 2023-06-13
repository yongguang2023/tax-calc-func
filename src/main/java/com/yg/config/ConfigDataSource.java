package com.yg.config;

import java.lang.annotation.*;

/**
 * 初始化数据源注解
 * Created by lyf on 2019/7/1.
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigDataSource {
    String value() default "";
}
