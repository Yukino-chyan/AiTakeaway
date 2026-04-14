package com.aitakeaway.server.controller;

import com.aitakeaway.server.common.Result;
import com.aitakeaway.server.entity.Merchant;
import com.aitakeaway.server.service.MerchantService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new RuntimeException("请先登录");
    }

    // ==================== 商家端接口 ====================

    @GetMapping("/my-shop")
    public Result<Merchant> getMyShop() {
        Long userId = getCurrentUserId();
        Merchant shop = merchantService.getMyShop(userId);
        if (shop == null) {
            return Result.error("您还没有创建店铺");
        }
        return Result.success(shop);
    }

    @PostMapping("/create")
    public Result<Void> createShop(@RequestBody CreateShopRequest request) {
        Long userId = getCurrentUserId();
        Merchant merchant = new Merchant();
        merchant.setName(request.getName());
        merchant.setPhone(request.getPhone());
        merchant.setAddress(request.getAddress());
        merchant.setBusinessHours(request.getBusinessHours());
        merchant.setDeliveryRange(request.getDeliveryRange());
        merchant.setDeliveryFee(request.getDeliveryFee());
        merchant.setDescription(request.getDescription());
        merchant.setAvatar(request.getAvatar());

        merchantService.createShop(merchant, userId);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateShop(@RequestBody UpdateShopRequest request) {
        Long userId = getCurrentUserId();
        Merchant merchant = new Merchant();
        merchant.setId(request.getId());
        merchant.setName(request.getName());
        merchant.setPhone(request.getPhone());
        merchant.setAddress(request.getAddress());
        merchant.setBusinessHours(request.getBusinessHours());
        merchant.setDeliveryRange(request.getDeliveryRange());
        merchant.setDeliveryFee(request.getDeliveryFee());
        merchant.setDescription(request.getDescription());
        merchant.setAvatar(request.getAvatar());

        merchantService.updateShop(merchant, userId);
        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(
            @RequestParam Long id,
            @RequestParam Integer status) {
        Long userId = getCurrentUserId();
        merchantService.updateStatus(id, status, userId);
        return Result.success();
    }

    // ==================== 用户端接口 ====================

    @GetMapping("/detail/{id}")
    public Result<Merchant> getDetail(@PathVariable Long id) {
        Merchant merchant = merchantService.getShopDetail(id);
        if (merchant == null) {
            return Result.error("店铺不存在");
        }
        return Result.success(merchant);
    }

    @GetMapping("/list")
    public Result<IPage<Merchant>> getShopList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(merchantService.getOpenShopList(page, size));
    }

    @GetMapping("/search")
    public Result<IPage<Merchant>> searchShop(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(merchantService.searchShop(keyword, page, size));
    }

    // ==================== 请求对象 ====================

    @Data
    static class CreateShopRequest {
        private String name;
        private String phone;
        private String address;
        private String businessHours;
        private Integer deliveryRange;
        private BigDecimal deliveryFee;
        private String description;
        private String avatar;
    }

    @Data
    static class UpdateShopRequest {
        private Long id;
        private String name;
        private String phone;
        private String address;
        private String businessHours;
        private Integer deliveryRange;
        private BigDecimal deliveryFee;
        private String description;
        private String avatar;
    }
}
