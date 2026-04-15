package com.aitakeaway.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号（唯一，便于展示） */
    private String orderNo;

    /** 下单用户ID */
    private Long userId;

    /** 商家ID */
    private Long merchantId;

    /**
     * 订单状态：
     * 0=待确认(PENDING)
     * 1=已确认(CONFIRMED)
     * 2=配送中(DELIVERING)
     * 3=已完成(COMPLETED)
     * 4=已取消(CANCELLED)
     */
    private Integer status;

    /** 商品总金额（不含配送费） */
    private BigDecimal totalAmount;

    /** 配送费快照（下单时记录，防止商家修改后影响老订单） */
    private BigDecimal deliveryFee;

    /** 实付金额 = totalAmount + deliveryFee */
    private BigDecimal payAmount;

    /** 收货地址 */
    private String deliveryAddress;

    /** 备注 */
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ==================== 状态常量 ====================
    public static final int STATUS_PENDING    = 0; // 待确认
    public static final int STATUS_CONFIRMED  = 1; // 已确认
    public static final int STATUS_DELIVERING = 2; // 配送中
    public static final int STATUS_COMPLETED  = 3; // 已完成
    public static final int STATUS_CANCELLED  = 4; // 已取消
}
