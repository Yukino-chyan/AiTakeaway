package com.aitakeaway.server.service.impl;

import com.aitakeaway.server.entity.Cart;
import com.aitakeaway.server.entity.Dish;
import com.aitakeaway.server.entity.Merchant;
import com.aitakeaway.server.mapper.CartMapper;
import com.aitakeaway.server.mapper.DishMapper;
import com.aitakeaway.server.service.DishService;
import com.aitakeaway.server.service.MerchantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private final MerchantService merchantService;
    private final CartMapper cartMapper;

    /** 根据 userId 获取其店铺，若不存在则抛异常 */
    private Merchant getMerchantByUserId(Long userId) {
        Merchant merchant = merchantService.getMyShop(userId);
        if (merchant == null) {
            throw new RuntimeException("您还没有创建店铺");
        }
        return merchant;
    }

    /** 校验菜品归属：菜品必须属于该用户的店铺 */
    private Dish getOwnedDish(Long dishId, Long merchantId) {
        Dish dish = getOne(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getId, dishId)
                .eq(Dish::getMerchantId, merchantId)
                .eq(Dish::getDeleted, 0));
        if (dish == null) {
            throw new RuntimeException("菜品不存在或您无权操作");
        }
        return dish;
    }

    @Override
    @Transactional
    public void createDish(Dish dish, Long userId) {
        Merchant merchant = getMerchantByUserId(userId);
        dish.setMerchantId(merchant.getId());
        dish.setStatus(Dish.STATUS_ON);
        save(dish);
    }

    @Override
    @Transactional
    public void updateDish(Dish dish, Long userId) {
        Merchant merchant = getMerchantByUserId(userId);
        Dish existing = getOwnedDish(dish.getId(), merchant.getId());

        if (dish.getName() != null)        existing.setName(dish.getName());
        if (dish.getPrice() != null)       existing.setPrice(dish.getPrice());
        if (dish.getImage() != null)       existing.setImage(dish.getImage());
        if (dish.getDescription() != null) existing.setDescription(dish.getDescription());
        if (dish.getCategory() != null)    existing.setCategory(dish.getCategory());

        updateById(existing);
    }

    @Override
    @Transactional
    public void updateStatus(Long dishId, Integer status, Long userId) {
        if (status != Dish.STATUS_ON && status != Dish.STATUS_OFF) {
            throw new RuntimeException("状态值不合法");
        }
        Merchant merchant = getMerchantByUserId(userId);
        Dish dish = getOwnedDish(dishId, merchant.getId());
        dish.setStatus(status);
        updateById(dish);
    }

    @Override
    @Transactional
    public void deleteDish(Long dishId, Long userId) {
        Merchant merchant = getMerchantByUserId(userId);
        getOwnedDish(dishId, merchant.getId());
        removeById(dishId);
        // 同步清理所有用户购物车中该菜品的记录
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getDishId, dishId));
    }

    @Override
    public List<Dish> getMyDishList(Long userId) {
        Merchant merchant = getMerchantByUserId(userId);
        return list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getMerchantId, merchant.getId())
                .eq(Dish::getDeleted, 0)
                .orderByDesc(Dish::getCreateTime));
    }

    @Override
    public List<Dish> getOnDishList(Long merchantId) {
        return list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getMerchantId, merchantId)
                .eq(Dish::getStatus, Dish.STATUS_ON)
                .eq(Dish::getDeleted, 0)
                .orderByDesc(Dish::getCreateTime));
    }

    @Override
    public List<Dish> searchOnDishes(String keyword, BigDecimal maxPrice) {
        return list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, Dish.STATUS_ON)
                .eq(Dish::getDeleted, 0))
                .stream()
                .filter(d -> keyword == null
                        || d.getName().contains(keyword)
                        || (d.getDescription() != null && d.getDescription().contains(keyword)))
                .filter(d -> maxPrice == null || d.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
    }
}
