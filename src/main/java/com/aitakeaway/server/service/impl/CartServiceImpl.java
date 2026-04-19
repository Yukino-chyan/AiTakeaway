package com.aitakeaway.server.service.impl;

import com.aitakeaway.server.dto.CartItemVO;
import com.aitakeaway.server.dto.CartVO;
import com.aitakeaway.server.entity.Cart;
import com.aitakeaway.server.entity.Dish;
import com.aitakeaway.server.entity.Merchant;
import com.aitakeaway.server.mapper.CartMapper;
import com.aitakeaway.server.service.CartService;
import com.aitakeaway.server.service.DishService;
import com.aitakeaway.server.service.MerchantService;
import com.aitakeaway.server.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final DishService dishService;
    private final OrderService orderService;
    private final MerchantService merchantService;

    @Override
    @Transactional
    public void addToCart(Long dishId, Integer quantity, Long userId) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("数量必须大于0");
        }

        // 校验菜品是否存在且已上架
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        if (dish.getStatus() == Dish.STATUS_OFF) {
            throw new RuntimeException("菜品已下架，无法加入购物车");
        }

        // 跨商家检查：购物车不为空时，新菜品必须属于同一商家
        List<Cart> existing = list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
        if (!existing.isEmpty() && !existing.get(0).getMerchantId().equals(dish.getMerchantId())) {
            throw new RuntimeException("购物车中已有其他商家的菜品，请先清空购物车再下单");
        }

        // 检查购物车中是否已存在该菜品
        Cart existingCart = getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDishId, dishId));

        if (existingCart != null) {
            // 已存在则累加数量
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            updateById(existingCart);
        } else {
            // 不存在则新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setDishId(dishId);
            cart.setDishName(dish.getName());
            cart.setDishImage(dish.getImage());
            cart.setDishPrice(dish.getPrice());
            cart.setMerchantId(dish.getMerchantId());
            cart.setQuantity(quantity);
            save(cart);
        }
    }

    @Override
    @Transactional
    public void updateQuantity(Long cartId, Integer quantity, Long userId) {
        Cart cart = getCartItem(cartId, userId);
        
        if (quantity <= 0) {
            // 数量为0或负数时删除
            removeById(cartId);
        } else {
            cart.setQuantity(quantity);
            updateById(cart);
        }
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartId, Long userId) {
        Cart cart = getCartItem(cartId, userId);
        removeById(cartId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
    }

    @Override
    public CartVO getCartList(Long userId) {
        List<Cart> carts = list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime));

        CartVO vo = new CartVO();
        if (carts.isEmpty()) {
            vo.setItems(List.of());
            vo.setTotalAmount(BigDecimal.ZERO);
            return vo;
        }

        // 过滤掉已下架或已删除的菜品
        List<Cart> availableCarts = carts.stream()
                .filter(cart -> {
                    Dish dish = dishService.getById(cart.getDishId());
                    return dish != null && dish.getDeleted() != 1 && dish.getStatus() == Dish.STATUS_ON;
                })
                .collect(Collectors.toList());

        vo.setMerchantId(carts.get(0).getMerchantId());
        List<CartItemVO> items = availableCarts.stream().map(cart -> {
            CartItemVO item = new CartItemVO();
            item.setCartId(cart.getId());
            item.setDishId(cart.getDishId());
            item.setDishName(cart.getDishName());
            item.setDishImage(cart.getDishImage());
            item.setDishPrice(cart.getDishPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(cart.getDishPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            return item;
        }).collect(Collectors.toList());

        vo.setItems(items);
        vo.setTotalAmount(items.stream()
                .map(CartItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return vo;
    }

    @Override
    @Transactional
    public Long createOrderFromCart(Long userId, Long merchantId, String deliveryAddress, String remark) {
        // 1. 基本参数校验
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new RuntimeException("收货地址不能为空");
        }

        // 2. 只取该商家的购物车商品（@TableLogic 自动过滤已删除记录）
        List<Cart> cartList = list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getMerchantId, merchantId));

        if (cartList == null || cartList.isEmpty()) {
            throw new RuntimeException("该商家购物车为空");
        }

        // 3. 校验商家是否存在且营业
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }
        if (merchant.getStatus() != Merchant.STATUS_OPEN) {
            throw new RuntimeException("该商家暂未营业");
        }

        // 4. 构建订单项Map、价格快照并校验菜品
        Map<Long, Integer> items = new HashMap<>();
        Map<Long, BigDecimal> snapshotPrices = new HashMap<>();
        for (Cart cart : cartList) {
            Dish dish = dishService.getById(cart.getDishId());
            if (dish == null || dish.getDeleted() == 1) {
                throw new RuntimeException("菜品「" + cart.getDishName() + "」已下架或不存在");
            }
            if (dish.getStatus() != Dish.STATUS_ON) {
                throw new RuntimeException("菜品「" + cart.getDishName() + "」已下架");
            }
            items.put(cart.getDishId(), cart.getQuantity());
            snapshotPrices.put(cart.getDishId(), cart.getDishPrice());
        }

        // 5. 调用订单服务创建订单（使用购物车价格快照）
        Long orderId = orderService.placeOrder(userId, merchantId, deliveryAddress, remark, items, snapshotPrices);

        // 6. 只清除该商家的购物车条目
        remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getMerchantId, merchantId));

        return orderId;
    }

    /**
     * 获取购物车项并校验归属
     */
    private Cart getCartItem(Long cartId, Long userId) {
        Cart cart = getById(cartId);
        if (cart == null) {
            throw new RuntimeException("购物车项不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该购物车项");
        }
        return cart;
    }
}
