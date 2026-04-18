package com.aitakeaway.server.controller;

import com.aitakeaway.server.common.Result;
import com.aitakeaway.server.dto.CartVO;
import com.aitakeaway.server.service.CartService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new RuntimeException("请先登录");
    }

    /**
     * 添加菜品到购物车
     */
    @PostMapping("/add")
    public Result<Void> addToCart(@RequestBody AddToCartRequest request) {
        cartService.addToCart(request.getDishId(), request.getQuantity(), getCurrentUserId());
        return Result.success();
    }

    /**
     * 修改购物车中菜品数量
     */
    @PutMapping("/update-quantity")
    public Result<Void> updateQuantity(@RequestBody UpdateQuantityRequest request) {
        cartService.updateQuantity(request.getCartId(), request.getQuantity(), getCurrentUserId());
        return Result.success();
    }

    /**
     * 从购物车删除菜品
     */
    @DeleteMapping("/{cartId}")
    public Result<Void> removeFromCart(@PathVariable Long cartId) {
        cartService.removeFromCart(cartId, getCurrentUserId());
        return Result.success();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<Void> clearCart() {
        cartService.clearCart(getCurrentUserId());
        return Result.success();
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Result<CartVO> getCartList() {
        return Result.success(cartService.getCartList(getCurrentUserId()));
    }

    /**
     * 从购物车创建订单（联动订单模块）
     */
    @PostMapping("/create-order")
    public Result<Long> createOrderFromCart(@RequestBody CreateOrderRequest request) {
        Long orderId = cartService.createOrderFromCart(
                getCurrentUserId(),
                request.getMerchantId(),
                request.getDeliveryAddress(),
                request.getRemark()
        );
        return Result.success(orderId);
    }

    // ==================== 请求对象 ====================

    @Data
    static class AddToCartRequest {
        /** 菜品ID */
        private Long dishId;
        /** 数量 */
        private Integer quantity;
    }

    @Data
    static class UpdateQuantityRequest {
        /** 购物车ID */
        private Long cartId;
        /** 数量 */
        private Integer quantity;
    }

    @Data
    static class CreateOrderRequest {
        /** 商家ID */
        private Long merchantId;
        /** 收货地址 */
        private String deliveryAddress;
        /** 备注 */
        private String remark;
    }
}
