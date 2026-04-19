package com.aitakeaway.server.service;

import com.aitakeaway.server.dto.OrderDetailVO;
import com.aitakeaway.server.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    /**
     * 用户下单
     *
     * @param userId          下单用户ID
     * @param merchantId      商家ID
     * @param deliveryAddress 收货地址
     * @param remark          备注
     * @param items           {dishId -> quantity}
     * @param snapshotPrices  购物车价格快照 {dishId -> price}，为 null 时使用菜品实时价格
     * @return 新订单ID
     */
    Long placeOrder(Long userId, Long merchantId, String deliveryAddress,
                    String remark, Map<Long, Integer> items, Map<Long, BigDecimal> snapshotPrices);

    /** 直接下单（无价格快照，使用实时菜品价格） */
    default Long placeOrder(Long userId, Long merchantId, String deliveryAddress,
                            String remark, Map<Long, Integer> items) {
        return placeOrder(userId, merchantId, deliveryAddress, remark, items, null);
    }

    /** 用户取消订单（仅允许 PENDING 状态） */
    void cancelOrder(Long orderId, Long userId);

    /** 获取订单详情（含订单行）——用户和商家均可调用，需校验权限 */
    OrderDetailVO getOrderDetail(Long orderId, Long currentUserId, String role);

    /** 用户查询自己的订单列表 */
    List<Order> getMyOrders(Long userId);

    // ==================== 商家端状态流转 ====================

    /** 商家确认订单：PENDING → CONFIRMED */
    void confirmOrder(Long orderId, Long merchantUserId);

    /** 商家开始配送：CONFIRMED → DELIVERING */
    void deliverOrder(Long orderId, Long merchantUserId);

    /** 商家完成订单：DELIVERING → COMPLETED */
    void completeOrder(Long orderId, Long merchantUserId);

    /** 商家查询自己店铺的订单列表（可按状态过滤） */
    List<Order> getMerchantOrders(Long merchantUserId, Integer status);
}
