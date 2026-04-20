package com.aitakeaway.server.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MerchantCartVO {

    private Long merchantId;
    private String merchantName;
    private List<CartItemVO> items;
    private BigDecimal merchantTotal;
}
