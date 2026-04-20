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
        List<Dish> all = list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, Dish.STATUS_ON)
                .eq(Dish::getDeleted, 0));

        return all.stream()
                .filter(d -> matchesKeyword(d, keyword))
                .filter(d -> maxPrice == null || d.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(Dish d, String raw) {
        if (raw == null || raw.isBlank()) return true;
        // 去掉语气词，保留核心词
        String cleaned = raw.replaceAll("[的了吗呢啊哦嗯些点一下吧]", "").trim();
        if (cleaned.isEmpty()) return true;

        String name = d.getName() == null ? "" : d.getName();
        String desc = d.getDescription() == null ? "" : d.getDescription();
        String cat  = d.getCategory() == null ? "" : d.getCategory();
        String all  = name + desc + cat;

        // 第一优先级：整体关键词子串匹配（精确）
        if (all.contains(cleaned)) return true;

        // 第二优先级：逗号分割后每段子串匹配（处理"酸,辣"这类提取词）
        for (String token : cleaned.split("[,，、\\s]+")) {
            if (!token.isEmpty() && all.contains(token)) return true;
        }

        // 第三优先级：2字以上关键词拆成2字滑窗匹配，避免单字误匹配
        if (cleaned.length() >= 2) {
            for (int i = 0; i <= cleaned.length() - 2; i++) {
                String bigram = cleaned.substring(i, i + 2);
                if (all.contains(bigram)) return true;
            }
        }

        return false;
    }
}
