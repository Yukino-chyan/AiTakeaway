package com.aitakeaway.server.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartVO {

    /** 按商家分组的购物车列表 */
    private List<MerchantCartVO> merchants;

    /** 所有商家合计金额 */
    private BigDecimal totalAmount;
}
