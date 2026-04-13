package com.aitakeaway.server.service;

import com.aitakeaway.server.entity.Merchant;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商家服务接口
 */
public interface MerchantService extends IService<Merchant> {

    /**
     * 创建店铺
     * @param merchant 商家信息
     * @param userId 用户ID（商家账号）
     */
    void createShop(Merchant merchant, Long userId);

    /**
     * 更新店铺信息
     * @param merchant 商家信息
     * @param userId 用户ID（用于校验权限）
     */
    void updateShop(Merchant merchant, Long userId);

    /**
     * 获取商家自己的店铺信息
     * @param userId 用户ID
     * @return 店铺信息
     */
    Merchant getMyShop(Long userId);

    /**
     * 获取店铺详情
     * @param merchantId 店铺ID
     * @return 店铺信息
     */
    Merchant getShopDetail(Long merchantId);

    /**
     * 修改营业状态
     * @param merchantId 店铺ID
     * @param status 状态：0休息中 1营业中
     * @param userId 用户ID（用于校验权限）
     */
    void updateStatus(Long merchantId, Integer status, Long userId);

    /**
     * 分页查询营业中的店铺列表
     * @param page 页码
     * @param size 每页数量
     * @return 店铺分页列表
     */
    IPage<Merchant> getOpenShopList(Integer page, Integer size);

    /**
     * 搜索店铺（按名称模糊查询）
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 店铺分页列表
     */
    IPage<Merchant> searchShop(String keyword, Integer page, Integer size);
}