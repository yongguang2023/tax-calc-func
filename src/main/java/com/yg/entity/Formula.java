package com.yg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName(value = "tax_formula")
public class Formula {

    /**
     *  主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *  公式名称
     */
    private String name;

    /**
     *  公式函数名称
     */
    private String funcName;

    /**
     *  公式内容
     */
    private String content;

    /**
     *  公式编译内容
     */
    private byte[] compileContent;

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
