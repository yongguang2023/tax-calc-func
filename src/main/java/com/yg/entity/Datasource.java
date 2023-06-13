package com.yg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "tax_datasource")
public class Datasource {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *  数据源名称
     */
    private String name;

    /**
     *  数据源url
     */
    private String url;

    /**
     *  用户名
     */
    private String username;

    /**
     *  密码
     */
    private String password;

    /**
     *  分表策略表达式
     */
    private String tableExp;

    /**
     *  分表数据
     */
    private String tables;

    /**
     *  更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     *  更新人
     */
    private String updateName;
}
