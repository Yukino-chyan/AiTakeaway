package com.aitakeaway.server.service.ai;

import com.aitakeaway.server.entity.Dish;
import com.aitakeaway.server.entity.Merchant;
import com.aitakeaway.server.service.CartService;
import com.aitakeaway.server.service.DishService;
import com.aitakeaway.server.service.MerchantService;
import com.aitakeaway.server.service.ReviewService;
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
    private final ReviewService reviewService;

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

    @Tool("跨所有商家搜索上架菜品。用户表达口味、偏好或生活场景时调用此工具。不确定搜什么时传空字符串获取随机推荐")
    public String searchDishesGlobal(
            @P("搜索关键词，不确定时传空字符串") String keyword,
            @P("最高价格（元），无限制时传0") double maxPrice) {

        BigDecimal maxPriceDecimal = maxPrice <= 0 ? null : BigDecimal.valueOf(maxPrice);
        List<Dish> dishes = dishService.searchOnDishes(
                (keyword == null || keyword.isBlank()) ? null : keyword,
                maxPriceDecimal);

        // 兜底：搜不到结果时随机返回3道菜作为推荐
        if (dishes.isEmpty()) {
            dishes = dishService.searchOnDishes(null, null);
            if (dishes.isEmpty()) return "暂时没有可用菜品";
            java.util.Collections.shuffle(dishes);
            dishes = dishes.stream().limit(3).collect(Collectors.toList());
            return "没有找到完全匹配的菜品，为您随机推荐：\n" + formatDishes(dishes);
        }

        return formatDishes(dishes.stream().limit(10).collect(Collectors.toList()));
    }

    private String formatDishes(List<Dish> dishes) {
        return dishes.stream()
                .map(d -> {
                    Merchant m = merchantService.getById(d.getMerchantId());
                    String merchantName = m != null ? m.getName() : "未知商家";
                    Double avg = reviewService.getAvgRating(d.getMerchantId());
                    long cnt = reviewService.getReviewCount(d.getMerchantId());
                    String ratingStr = avg != null ? String.format(" ⭐%.1f(%d评)", avg, cnt) : " (暂无评价)";
                    return String.format("菜品ID:%d 【%s】¥%.2f 来自:【%s】(商家ID:%d)%s%s",
                            d.getId(), d.getName(), d.getPrice(),
                            merchantName, d.getMerchantId(),
                            ratingStr,
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
