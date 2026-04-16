package com.aitakeaway.server.service.impl;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 校验菜品是否存在且已上架
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        if (dish.getStatus() == Dish.STATUS_OFF) {
            throw new RuntimeException("菜品已下架，无法加入购物车");
        }

        // 检查购物车中是否已存在该菜品
        Cart existingCart = getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDishId, dishId)
                .eq(Cart::getDeleted, 0));

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
    public List<Cart> getCartList(Long userId) {
        return list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDeleted, 0)
                .orderByDesc(Cart::getCreateTime));
    }

    @Override
    @Transactional
    public Long createOrderFromCart(Long userId, String deliveryAddress, String remark) {
        // 1. 获取用户的购物车列表
        List<Cart> cartList = getCartList(userId);
        if (cartList == null || cartList.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        // 2. 获取商家信息（同一订单只能来自同一商家）
        Long merchantId = cartList.get(0).getMerchantId();
        
        // 校验商家是否存在且营业
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null || merchant.getDeleted() == 1) {
            throw new RuntimeException("商家不存在");
        }
        if (merchant.getStatus() != Merchant.STATUS_OPEN) {
            throw new RuntimeException("该商家暂未营业");
        }

        // 3. 构建订单项Map并校验菜品
        Map<Long, Integer> items = new HashMap<>();
        for (Cart cart : cartList) {
            Dish dish = dishService.getById(cart.getDishId());
            if (dish == null || dish.getDeleted() == 1) {
                throw new RuntimeException("菜品「" + cart.getDishName() + "」已下架或不存在");
            }
            if (dish.getStatus() != Dish.STATUS_ON) {
                throw new RuntimeException("菜品「" + cart.getDishName() + "」已下架");
            }
            if (!dish.getMerchantId().equals(merchantId)) {
                throw new RuntimeException("不支持跨商家下单，请分开结算");
            }
            items.put(cart.getDishId(), cart.getQuantity());
        }

        // 4. 调用订单服务创建订单
        Long orderId = orderService.placeOrder(userId, merchantId, deliveryAddress, remark, items);

        // 5. 清空购物车
        clearCart(userId);

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
