package com.aitakeaway.server.dto;

import com.aitakeaway.server.entity.Order;
import com.aitakeaway.server.entity.OrderItem;
import lombok.Data;

import java.util.List;

/**
 * 订单详情视图对象（订单头 + 订单行）
 */
@Data
public class OrderDetailVO {

    private Order order;
    private List<OrderItem> items;
}
