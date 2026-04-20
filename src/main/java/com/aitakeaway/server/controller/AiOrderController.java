package com.aitakeaway.server.controller;

import com.aitakeaway.server.common.Result;
import com.aitakeaway.server.config.AiConfig;
import com.aitakeaway.server.entity.Dish;
import com.aitakeaway.server.entity.Merchant;
import com.aitakeaway.server.service.DishService;
import com.aitakeaway.server.service.MerchantService;
import com.aitakeaway.server.service.ReviewService;
import com.aitakeaway.server.service.ai.AiOrderAssistant;
import com.aitakeaway.server.service.ai.AiResponseFormatter;
import com.aitakeaway.server.service.ai.IntentRouter;
import com.aitakeaway.server.service.ai.UserContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiOrderController {

    private final AiOrderAssistant aiOrderAssistant;
    private final AiResponseFormatter aiResponseFormatter;
    private final AiConfig aiConfig;
    private final IntentRouter intentRouter;
    private final DishService dishService;
    private final MerchantService merchantService;
    private final ReviewService reviewService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return Result.error("消息不能为空");
        }
        Long userId = getCurrentUserId();
        String message = request.getMessage().trim();

        try {
            UserContext.set(userId);

            // Step 1: classify intent via LLM
            String intent;
            try {
                intent = intentRouter.classify(message);
            } catch (Exception e) {
                intent = "OTHER";
            }

            // Step 2: dispatch
            if (intent != null && intent.startsWith("SEARCH")) {
                return handleSearch(userId, message, intent);
            } else {
                return handleOther(userId, message);
            }
        } finally {
            UserContext.clear();
        }
    }

    private Result<String> handleSearch(Long userId, String originalMessage, String intent) {
        // Extract keyword from "SEARCH|keyword" or "SEARCH"
        String keyword = "";
        if (intent.contains("|")) {
            keyword = intent.substring(intent.indexOf('|') + 1).trim();
        }

        // Java directly queries DB — no tool call needed
        List<Dish> dishes = dishService.searchOnDishes(
                keyword.isBlank() ? null : keyword, null);

        String searchResults;
        if (dishes.isEmpty()) {
            // 兜底：全量随机，跨商家分散
            List<Dish> all = dishService.searchOnDishes(null, null);
            if (all.isEmpty()) {
                searchResults = "（暂无可用菜品）";
            } else {
                searchResults = "（未找到精确匹配，随机推荐）\n" + formatDishes(diversify(all, 6));
            }
        } else {
            searchResults = formatDishes(diversify(dishes, 9));
        }

        String enrichedMessage = originalMessage + "\n\n【平台搜索结果】\n" + searchResults;

        try {
            // 使用无工具的 formatter，彻底避免 tool_calls 格式问题
            return Result.success(aiResponseFormatter.format(userId, enrichedMessage));
        } catch (Exception e) {
            return Result.error("AI助手暂时遇到问题，请稍后重试");
        }
    }

    private Result<String> handleOther(Long userId, String message) {
        try {
            return Result.success(aiOrderAssistant.chat(userId, message));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("tool_calls")) {
                aiConfig.clearUserMemory(userId);
                try {
                    return Result.success(aiOrderAssistant.chat(userId, message));
                } catch (Exception ignore) {}
            }
            return Result.error("AI助手暂时遇到问题，请稍后重试");
        }
    }

    /**
     * 打乱顺序后按商家分散取菜：每家最多 maxPerMerchant 道，总数不超过 total。
     * 保证结果来自尽可能多的不同商家。
     */
    private List<Dish> diversify(List<Dish> dishes, int total) {
        java.util.Collections.shuffle(dishes);
        java.util.Map<Long, Integer> countPerMerchant = new java.util.HashMap<>();
        int maxPerMerchant = Math.max(1, (int) Math.ceil((double) total / 3));
        List<Dish> result = new java.util.ArrayList<>();
        for (Dish d : dishes) {
            if (result.size() >= total) break;
            int cnt = countPerMerchant.getOrDefault(d.getMerchantId(), 0);
            if (cnt < maxPerMerchant) {
                result.add(d);
                countPerMerchant.put(d.getMerchantId(), cnt + 1);
            }
        }
        // 若结果不足（商家数少），放宽限制补齐
        if (result.size() < total) {
            for (Dish d : dishes) {
                if (result.size() >= total) break;
                if (!result.contains(d)) result.add(d);
            }
        }
        return result;
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

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new RuntimeException("请先登录");
    }

    @Data
    static class ChatRequest {
        private String message;
    }
}
