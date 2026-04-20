package com.aitakeaway.server.controller;

import com.aitakeaway.server.common.Result;
import com.aitakeaway.server.config.AiConfig;
import com.aitakeaway.server.service.ai.AiOrderAssistant;
import com.aitakeaway.server.service.ai.UserContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiOrderController {

    private final AiOrderAssistant aiOrderAssistant;
    private final AiConfig aiConfig;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return Result.error("消息不能为空");
        }
        Long userId = getCurrentUserId();
        try {
            UserContext.set(userId);
            try {
                return Result.success(aiOrderAssistant.chat(userId, request.getMessage()));
            } catch (Exception e) {
                // 对话历史损坏时清空记忆重试一次
                if (e.getMessage() != null && e.getMessage().contains("tool_calls")) {
                    aiConfig.clearUserMemory(userId);
                    try {
                        return Result.success(aiOrderAssistant.chat(userId, request.getMessage()));
                    } catch (Exception retryEx) {
                        return Result.error("AI助手暂时遇到问题，请稍后重试");
                    }
                }
                return Result.error("AI助手暂时遇到问题，请稍后重试");
            }
        } finally {
            UserContext.clear();
        }
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
