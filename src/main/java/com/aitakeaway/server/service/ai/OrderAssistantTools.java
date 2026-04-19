package com.aitakeaway.server.service.ai;

import com.aitakeaway.server.entity.Dish;
import com.aitakeaway.server.service.CartService;
import com.aitakeaway.server.service.DishService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderAssistantTools {

    private final DishService dishService;
    private final CartService cartService;

    @Tool("查询指定商家的上架菜品列表。可按关键词（菜名或描述）和最高价格筛选，不限制时传 null")
    public String queryDishes(
            @P("商家ID") Long merchantId,
            @P("搜索关键词，不限制传 null") String keyword,
            @P("最高价格（元），不限制传 null") BigDecimal maxPrice) {

        List<Dish> dishes = dishService.getOnDishList(merchantId).stream()
                .filter(d -> keyword == null
                        || d.getName().contains(keyword)
                        || (d.getDescription() != null && d.getDescription().contains(keyword)))
                .filter(d -> maxPrice == null || d.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());

        if (dishes.isEmpty()) {
            return "没有找到符合条件的菜品";
        }

        return dishes.stream()
                .map(d -> String.format("菜品ID:%d 【%s】¥%.2f %s",
                        d.getId(),
                        d.getName(),
                        d.getPrice(),
                        d.getDescription() != null ? "- " + d.getDescription() : ""))
                .collect(Collectors.joining("\n"));
    }

    @Tool("将指定菜品加入用户购物车。成功时返回确认信息，失败时返回失败原因")
    public String addToCart(
            @P("用户ID") Long userId,
            @P("菜品ID") Long dishId,
            @P("加入数量") Integer quantity) {
        try {
            cartService.addToCart(dishId, quantity, userId);
            return "已成功将菜品(ID:" + dishId + ")×" + quantity + " 加入购物车";
        } catch (RuntimeException e) {
            return "加入购物车失败：" + e.getMessage();
        }
    }
}
