package com.aitakeaway.server.service.ai;

import com.aitakeaway.server.entity.Dish;
import com.aitakeaway.server.entity.Merchant;
import com.aitakeaway.server.service.CartService;
import com.aitakeaway.server.service.DishService;
import com.aitakeaway.server.service.MerchantService;
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
    private final MerchantService merchantService;

    @Tool("查询指定商家的上架菜品列表。可按关键词（菜名或描述）和最高价格筛选")
    public String queryDishes(
            @P("商家ID") Long merchantId,
            @P("搜索关键词，无限制时传空字符串") String keyword,
            @P("最高价格（元），无限制时传0") double maxPrice) {

        List<Dish> dishes = dishService.getOnDishList(merchantId).stream()
                .filter(d -> keyword == null || keyword.isBlank()
                        || d.getName().contains(keyword)
                        || (d.getDescription() != null && d.getDescription().contains(keyword)))
                .filter(d -> maxPrice <= 0 || d.getPrice().compareTo(BigDecimal.valueOf(maxPrice)) <= 0)
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

    @Tool("跨所有商家搜索上架菜品。用户表达口味或类型偏好但未指定商家时优先调用此工具，结果含商家名称和商家ID")
    public String searchDishesGlobal(
            @P("搜索关键词，必须提供") String keyword,
            @P("最高价格（元），无限制时传0") double maxPrice) {

        BigDecimal maxPriceDecimal = maxPrice <= 0 ? null : BigDecimal.valueOf(maxPrice);
        List<Dish> dishes = dishService.searchOnDishes(keyword, maxPriceDecimal);
        if (dishes.isEmpty()) {
            return "没有找到符合条件的菜品";
        }

        return dishes.stream()
                .limit(10)
                .map(d -> {
                    Merchant m = merchantService.getById(d.getMerchantId());
                    String merchantName = m != null ? m.getName() : "未知商家";
                    return String.format("菜品ID:%d 【%s】¥%.2f 来自:【%s】(商家ID:%d)%s",
                            d.getId(), d.getName(), d.getPrice(),
                            merchantName, d.getMerchantId(),
                            d.getDescription() != null ? " - " + d.getDescription() : "");
                })
                .collect(Collectors.joining("\n"));
    }

    @Tool("将指定菜品加入当前用户的购物车。成功时返回确认信息，失败时返回失败原因")
    public String addToCart(
            @P("菜品ID") Long dishId,
            @P("加入数量") Integer quantity) {
        Long userId = UserContext.get();
        if (userId == null) {
            return "加入购物车失败：用户未登录";
        }
        try {
            cartService.addToCart(dishId, quantity, userId);
            return "已成功将菜品(ID:" + dishId + ")×" + quantity + " 加入购物车";
        } catch (RuntimeException e) {
            return "加入购物车失败：" + e.getMessage();
        }
    }
}
