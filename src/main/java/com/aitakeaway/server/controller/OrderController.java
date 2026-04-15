package com.aitakeaway.server.controller;

import com.aitakeaway.server.common.Result;
import com.aitakeaway.server.dto.OrderDetailVO;
import com.aitakeaway.server.entity.Order;
import com.aitakeaway.server.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new RuntimeException("请先登录");
    }

    /** 获取当前用户的第一个角色字符串（如 "ROLE_CUSTOMER"） */
    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
    }

    // ==================== 用户端接口 ====================

    /** 下单 */
    @PostMapping("/place")
    public Result<Long> placeOrder(@RequestBody PlaceOrderRequest request) {
        Long orderId = orderService.placeOrder(
                getCurrentUserId(),
                request.getMerchantId(),
                request.getDeliveryAddress(),
                request.getRemark(),
                request.getItems()
        );
        return Result.success(orderId);
    }

    /** 取消订单（仅 PENDING 状态可取消） */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id, getCurrentUserId());
        return Result.success();
    }

    /** 查询我的订单列表 */
    @GetMapping("/my-list")
    public Result<List<Order>> getMyOrders() {
        return Result.success(orderService.getMyOrders(getCurrentUserId()));
    }

    /** 查询订单详情（含订单行） */
    @GetMapping("/{id}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id, getCurrentUserId(), getCurrentRole()));
    }

    // ==================== 商家端接口 ====================

    /** 查询店铺订单列表，可按 status 过滤 */
    @GetMapping("/merchant-list")
    public Result<List<Order>> getMerchantOrders(
            @RequestParam(required = false) Integer status) {
        return Result.success(orderService.getMerchantOrders(getCurrentUserId(), status));
    }

    /** 确认订单：PENDING → CONFIRMED */
    @PutMapping("/{id}/confirm")
    public Result<Void> confirmOrder(@PathVariable Long id) {
        orderService.confirmOrder(id, getCurrentUserId());
        return Result.success();
    }

    /** 开始配送：CONFIRMED → DELIVERING */
    @PutMapping("/{id}/deliver")
    public Result<Void> deliverOrder(@PathVariable Long id) {
        orderService.deliverOrder(id, getCurrentUserId());
        return Result.success();
    }

    /** 完成订单：DELIVERING → COMPLETED */
    @PutMapping("/{id}/complete")
    public Result<Void> completeOrder(@PathVariable Long id) {
        orderService.completeOrder(id, getCurrentUserId());
        return Result.success();
    }

    // ==================== 请求对象 ====================

    @Data
    static class PlaceOrderRequest {
        /** 商家ID */
        private Long merchantId;
        /** 收货地址 */
        private String deliveryAddress;
        /** 备注 */
        private String remark;
        /** 购物车：{dishId -> quantity} */
        private Map<Long, Integer> items;
    }
}
