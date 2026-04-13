package com.aitakeaway.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的用户ID（商家账号） */
    private Long userId;

    /** 店铺名称 */
    private String name;

    /** 店铺头像URL */
    private String avatar;

    /** 联系电话 */
    private String phone;

    /** 店铺地址 */
    private String address;

    /** 营业时间，如：09:00-21:00 */
    private String businessHours;

    /** 配送范围（km） */
    private Integer deliveryRange;

    /** 配送费 */
    private BigDecimal deliveryFee;

    /** 店铺描述 */
    private String description;

    /** 状态：0休息中 1营业中 */
    private Integer status;

    /** 逻辑删除：0正常 1已删除 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ==================== 状态常量 ====================
    public static final int STATUS_REST = 0;     // 休息中
    public static final int STATUS_OPEN = 1;    // 营业中
}