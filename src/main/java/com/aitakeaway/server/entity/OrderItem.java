package com.aitakeaway.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属订单ID */
    private Long orderId;

    /** 菜品ID */
    private Long dishId;

    /** 菜品名称快照（防止菜品改名后影响历史订单展示） */
    private String dishName;

    /** 菜品单价快照 */
    private BigDecimal dishPrice;

    /** 购买数量 */
    private Integer quantity;

    /** 小计 = dishPrice * quantity */
    private BigDecimal subtotal;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
