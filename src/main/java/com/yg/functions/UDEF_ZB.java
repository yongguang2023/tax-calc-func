package com.yg.functions;


import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.yg.config.ConfigDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 *  获取上期值
 *
 *  @Service 注解会将公式自动注入spring容器，必须声明容器名称(也就是公式名称)
 */
@Service("UDEF_ZB")
public class UDEF_ZB extends AbstractFunction {

    @ConfigDataSource("配置的数据源名称")
    private JdbcTemplate jdbcTemplate;

    /**
     *  公式名称
     * @return
     */
    @Override
    public String getName() {
        return "UDEF_ZB";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        //  访问数据库 参照 jdbcTemplate 相关api
        jdbcTemplate.queryForList("select * from tb_user");
        return super.call(env, arg1, arg2, arg3);
    }
}
