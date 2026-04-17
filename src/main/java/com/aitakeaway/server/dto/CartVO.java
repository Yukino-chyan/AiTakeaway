package com.aitakeaway.server.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartVO {

    private Long merchantId;
    private List<CartItemVO> items;
    private BigDecimal totalAmount;
}
