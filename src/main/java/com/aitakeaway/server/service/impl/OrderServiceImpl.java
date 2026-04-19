package com.aitakeaway.server.service.impl;

import com.aitakeaway.server.dto.OrderDetailVO;
import com.aitakeaway.server.entity.Dish;
import com.aitakeaway.server.entity.Merchant;
import com.aitakeaway.server.entity.Order;
import com.aitakeaway.server.entity.OrderItem;
import com.aitakeaway.server.mapper.OrderItemMapper;
import com.aitakeaway.server.mapper.OrderMapper;
import com.aitakeaway.server.service.DishService;
import com.aitakeaway.server.service.MerchantService;
import com.aitakeaway.server.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final DishService     dishService;
    private final MerchantService merchantService;

    // ==================== 私有工具方法 ====================

    private static final AtomicLong SEQ = new AtomicLong(0);

    /** 生成唯一订单号：yyyyMMddHHmmss + 自增序列(4位) + 随机数(2位)，避免同一秒并发碰撞 */
    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = SEQ.incrementAndGet() % 10000;
        int rand = ThreadLocalRandom.current().nextInt(10, 99);
        return ts + String.format("%04d", seq) + rand;
    }

    /** 根据 userId 获取其商家信息，不存在则抛异常 */
    private Merchant getMerchantByUserId(Long userId) {
        Merchant merchant = merchantService.getMyShop(userId);
        if (merchant == null) {
            throw new RuntimeException("您还没有创建店铺");
        }
        return merchant;
    }

    /** 获取订单，不存在则抛异常 */
    private Order getExistingOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    /** 校验商家对订单的操作权限（订单必须属于该商家的店铺） */
    private void checkMerchantOwnership(Order order, Long merchantUserId) {
        Merchant merchant = getMerchantByUserId(merchantUserId);
        if (!order.getMerchantId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该订单");
        }
    }

    /** 校验订单状态是否为期望状态，不符合则抛异常 */
    private void checkStatus(Order order, int expectedStatus, String errorMsg) {
        if (!order.getStatus().equals(expectedStatus)) {
            throw new RuntimeException(errorMsg);
        }
    }

    // ==================== 接口实现 ====================

    @Override
    @Transactional
    public Long placeOrder(Long userId, Long merchantId, String deliveryAddress,
                           String remark, Map<Long, Integer> items, Map<Long, BigDecimal> snapshotPrices) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("订单中至少包含一个菜品");
        }

        // 1. 校验商家是否存在且营业
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null || merchant.getDeleted() == 1) {
            throw new RuntimeException("商家不存在");
        }
        if (merchant.getStatus() != Merchant.STATUS_OPEN) {
            throw new RuntimeException("该商家暂未营业");
        }

        // 2. 校验所有菜品，计算总金额
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Long dishId   = entry.getKey();
            Integer qty   = entry.getValue();

            if (qty == null || qty <= 0) {
                throw new RuntimeException("菜品数量必须大于0");
            }

            Dish dish = dishService.getById(dishId);
            if (dish == null || dish.getDeleted() == 1) {
                throw new RuntimeException("菜品(id=" + dishId + ")不存在");
            }
            if (!dish.getMerchantId().equals(merchantId)) {
                throw new RuntimeException("菜品(id=" + dishId + ")不属于该商家");
            }
            if (dish.getStatus() != Dish.STATUS_ON) {
                throw new RuntimeException("菜品「" + dish.getName() + "」已下架");
            }

            // 优先使用购物车快照价格，无快照时降级为实时价格
            BigDecimal price = (snapshotPrices != null && snapshotPrices.containsKey(dishId))
                    ? snapshotPrices.get(dishId)
                    : dish.getPrice();

            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setDishId(dishId);
            item.setDishName(dish.getName());
            item.setDishPrice(price);
            item.setQuantity(qty);
            item.setSubtotal(subtotal);
            orderItems.add(item);
        }

        // 3. 保存订单头
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setStatus(Order.STATUS_PENDING);
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(merchant.getDeliveryFee());
        order.setPayAmount(totalAmount.add(merchant.getDeliveryFee()));
        order.setDeliveryAddress(deliveryAddress);
        order.setRemark(remark);
        save(order);

        // 4. 保存订单行
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        return order.getId();
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getExistingOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        checkStatus(order, Order.STATUS_PENDING, "只有待确认的订单才能取消");
        order.setStatus(Order.STATUS_CANCELLED);
        updateById(order);
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId, Long currentUserId, String role) {
        Order order = getExistingOrder(orderId);

        // 权限校验：CUSTOMER 只能看自己的；MERCHANT 只能看自己店铺的
        if ("ROLE_CUSTOMER".equals(role)) {
            if (!order.getUserId().equals(currentUserId)) {
                throw new RuntimeException("无权查看该订单");
            }
        } else if ("ROLE_MERCHANT".equals(role)) {
            checkMerchantOwnership(order, currentUserId);
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));

        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrder(order);
        vo.setItems(orderItems);
        return vo;
    }

    @Override
    public List<Order> getMyOrders(Long userId) {
        return list(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getCreateTime));
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long merchantUserId) {
        Order order = getExistingOrder(orderId);
        checkMerchantOwnership(order, merchantUserId);
        checkStatus(order, Order.STATUS_PENDING, "只有待确认的订单才能确认");
        order.setStatus(Order.STATUS_CONFIRMED);
        updateById(order);
    }

    @Override
    @Transactional
    public void deliverOrder(Long orderId, Long merchantUserId) {
        Order order = getExistingOrder(orderId);
        checkMerchantOwnership(order, merchantUserId);
        checkStatus(order, Order.STATUS_CONFIRMED, "只有已确认的订单才能开始配送");
        order.setStatus(Order.STATUS_DELIVERING);
        updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, Long merchantUserId) {
        Order order = getExistingOrder(orderId);
        checkMerchantOwnership(order, merchantUserId);
        checkStatus(order, Order.STATUS_DELIVERING, "只有配送中的订单才能完成");
        order.setStatus(Order.STATUS_COMPLETED);
        updateById(order);
    }

    @Override
    public List<Order> getMerchantOrders(Long merchantUserId, Integer status) {
        Merchant merchant = getMerchantByUserId(merchantUserId);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchant.getId())
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        return list(wrapper);
    }
}
