package com.aitakeaway.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属商家ID */
    private Long merchantId;

    /** 菜品名称 */
    private String name;

    /** 价格 */
    private BigDecimal price;

    /** 图片URL */
    private String image;

    /** 描述 */
    private String description;

    /** 分类 */
    private String category;

    /** 状态：0=下架 1=上架 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public static final int STATUS_OFF = 0;
    public static final int STATUS_ON  = 1;
}
