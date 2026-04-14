package com.aitakeaway.server.service.impl;

import com.aitakeaway.server.entity.Merchant;
import com.aitakeaway.server.mapper.MerchantMapper;
import com.aitakeaway.server.service.MerchantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商家服务实现类
 */
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant>
        implements MerchantService {

    @Override
    @Transactional
    public void createShop(Merchant merchant, Long userId) {
        // 1. 检查是否已有店铺
        long count = count(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, userId)
                .eq(Merchant::getDeleted, 0));
        if (count > 0) {
            throw new RuntimeException("您已经创建过店铺了，每个商家只能创建一个店铺");
        }

        // 2. 设置默认值
        merchant.setUserId(userId);
        merchant.setStatus(Merchant.STATUS_OPEN);  // 默认营业中
        merchant.setDeliveryFee(merchant.getDeliveryFee() != null
                ? merchant.getDeliveryFee()
                : new java.math.BigDecimal("3.00"));  // 默认配送费3元
        merchant.setDeliveryRange(merchant.getDeliveryRange() != null
                ? merchant.getDeliveryRange()
                : 3);  // 默认配送范围3km
        merchant.setBusinessHours(merchant.getBusinessHours() != null
                ? merchant.getBusinessHours()
                : "09:00-21:00");  // 默认营业时间

        // 3. 保存
        save(merchant);
    }

    @Override
    @Transactional
    public void updateShop(Merchant merchant, Long userId) {
        // 1. 校验店铺是否存在且属于该用户
        Merchant existing = getOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getId, merchant.getId())
                .eq(Merchant::getUserId, userId)
                .eq(Merchant::getDeleted, 0));

        if (existing == null) {
            throw new RuntimeException("店铺不存在或您无权修改此店铺");
        }

        // 2. 更新字段（只更新允许修改的字段）
        if (merchant.getName() != null) {
            existing.setName(merchant.getName());
        }
        if (merchant.getAvatar() != null) {
            existing.setAvatar(merchant.getAvatar());
        }
        if (merchant.getPhone() != null) {
            existing.setPhone(merchant.getPhone());
        }
        if (merchant.getAddress() != null) {
            existing.setAddress(merchant.getAddress());
        }
        if (merchant.getBusinessHours() != null) {
            existing.setBusinessHours(merchant.getBusinessHours());
        }
        if (merchant.getDeliveryRange() != null) {
            existing.setDeliveryRange(merchant.getDeliveryRange());
        }
        if (merchant.getDeliveryFee() != null) {
            existing.setDeliveryFee(merchant.getDeliveryFee());
        }
        if (merchant.getDescription() != null) {
            existing.setDescription(merchant.getDescription());
        }

        // 3. 保存更新
        updateById(existing);
    }

    @Override
    public Merchant getMyShop(Long userId) {
        return getOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, userId)
                .eq(Merchant::getDeleted, 0));
    }

    @Override
    public Merchant getShopDetail(Long merchantId) {
        return getOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getId, merchantId)
                .eq(Merchant::getDeleted, 0));
    }

    @Override
    @Transactional
    public void updateStatus(Long merchantId, Integer status, Long userId) {
        // 校验权限
        Merchant merchant = getOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getId, merchantId)
                .eq(Merchant::getUserId, userId));

        if (merchant == null) {
            throw new RuntimeException("店铺不存在或您无权操作");
        }

        // 校验状态值
        if (status != Merchant.STATUS_OPEN && status != Merchant.STATUS_REST) {
            throw new RuntimeException("状态值不合法");
        }

        merchant.setStatus(status);
        updateById(merchant);
    }

    @Override
    public IPage<Merchant> getOpenShopList(Integer page, Integer size) {
        Page<Merchant> pageParam = new Page<>(page, size);
        return page(pageParam, new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getStatus, Merchant.STATUS_OPEN)
                .eq(Merchant::getDeleted, 0)
                .orderByDesc(Merchant::getCreateTime));
    }

    @Override
    public IPage<Merchant> searchShop(String keyword, Integer page, Integer size) {
        Page<Merchant> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();

        // 只搜索营业中的店铺
        wrapper.eq(Merchant::getStatus, Merchant.STATUS_OPEN)
                .eq(Merchant::getDeleted, 0);

        // 名称模糊搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Merchant::getName, keyword.trim());
        }

        wrapper.orderByDesc(Merchant::getCreateTime);

        return page(pageParam, wrapper);
    }
}