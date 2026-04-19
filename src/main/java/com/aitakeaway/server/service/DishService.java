package com.aitakeaway.server.service;

import com.aitakeaway.server.entity.Dish;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface DishService extends IService<Dish> {

    /** 新增菜品 */
    void createDish(Dish dish, Long userId);

    /** 编辑菜品 */
    void updateDish(Dish dish, Long userId);

    /** 上架/下架 */
    void updateStatus(Long dishId, Integer status, Long userId);

    /** 删除菜品 */
    void deleteDish(Long dishId, Long userId);

    /** 商家查询自己店铺的所有菜品 */
    List<Dish> getMyDishList(Long userId);

    /** 用户查询某商家的上架菜品 */
    List<Dish> getOnDishList(Long merchantId);

    /** 跨所有商家搜索上架菜品 */
    List<Dish> searchOnDishes(String keyword, BigDecimal maxPrice);
}
