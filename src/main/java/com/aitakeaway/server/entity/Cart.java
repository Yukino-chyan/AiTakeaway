package com.aitakeaway.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 */
@Data
@TableName("cart")
public class Cart {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 菜品ID */
    private Long dishId;

    /** 菜品名称（冗余字段，便于展示） */
    private String dishName;

    /** 菜品图片（冗余字段，便于展示） */
    private String dishImage;

    /** 菜品单价（冗余字段，便于展示） */
    private BigDecimal dishPrice;

    /** 数量 */
    private Integer quantity;

    /** 所属商家ID */
    private Long merchantId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
