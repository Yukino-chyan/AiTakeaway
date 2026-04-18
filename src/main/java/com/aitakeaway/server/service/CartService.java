package com.aitakeaway.server.service;

import com.aitakeaway.server.dto.CartVO;
import com.aitakeaway.server.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CartService extends IService<Cart> {

    /**
     * 添加菜品到购物车
     * @param dishId 菜品ID
     * @param quantity 数量
     * @param userId 用户ID
     */
    void addToCart(Long dishId, Integer quantity, Long userId);

    /**
     * 修改购物车中菜品数量
     * @param cartId 购物车ID
     * @param quantity 数量
     * @param userId 用户ID
     */
    void updateQuantity(Long cartId, Integer quantity, Long userId);

    /**
     * 从购物车删除菜品
     * @param cartId 购物车ID
     * @param userId 用户ID
     */
    void removeFromCart(Long cartId, Long userId);

    /**
     * 清空当前用户的购物车
     * @param userId 用户ID
     */
    void clearCart(Long userId);

    /**
     * 获取当前用户的购物车
     * @param userId 用户ID
     * @return 购物车VO
     */
    CartVO getCartList(Long userId);

    /**
     * 从购物车创建订单（仅结算指定商家的商品）
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @param deliveryAddress 收货地址
     * @param remark 备注
     * @return 新订单ID
     */
    Long createOrderFromCart(Long userId, Long merchantId, String deliveryAddress, String remark);
}
