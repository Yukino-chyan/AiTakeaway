package com.aitakeaway.server.controller;

import com.aitakeaway.server.common.Result;
import com.aitakeaway.server.entity.Dish;
import com.aitakeaway.server.service.DishService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new RuntimeException("请先登录");
    }

    // ==================== 商家端接口 ====================

    @PostMapping("/create")
    public Result<Void> createDish(@RequestBody DishRequest request) {
        Dish dish = buildDish(request);
        dishService.createDish(dish, getCurrentUserId());
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateDish(@RequestBody UpdateDishRequest request) {
        Dish dish = new Dish();
        dish.setId(request.getId());
        dish.setName(request.getName());
        dish.setPrice(request.getPrice());
        dish.setImage(request.getImage());
        dish.setDescription(request.getDescription());
        dish.setCategory(request.getCategory());
        dishService.updateDish(dish, getCurrentUserId());
        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        dishService.updateStatus(id, status, getCurrentUserId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id, getCurrentUserId());
        return Result.success();
    }

    @GetMapping("/my-list")
    public Result<List<Dish>> getMyDishList() {
        return Result.success(dishService.getMyDishList(getCurrentUserId()));
    }

    // ==================== 用户端接口 ====================

    @GetMapping("/list/{merchantId}")
    public Result<List<Dish>> getOnDishList(@PathVariable Long merchantId) {
        return Result.success(dishService.getOnDishList(merchantId));
    }

    // ==================== 请求对象 ====================

    @Data
    static class DishRequest {
        private String name;
        private BigDecimal price;
        private String image;
        private String description;
        private String category;
    }

    @Data
    static class UpdateDishRequest {
        private Long id;
        private String name;
        private BigDecimal price;
        private String image;
        private String description;
        private String category;
    }

    private Dish buildDish(DishRequest request) {
        Dish dish = new Dish();
        dish.setName(request.getName());
        dish.setPrice(request.getPrice());
        dish.setImage(request.getImage());
        dish.setDescription(request.getDescription());
        dish.setCategory(request.getCategory());
        return dish;
    }
}
