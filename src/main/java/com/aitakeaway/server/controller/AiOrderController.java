package com.aitakeaway.server.controller;

import com.aitakeaway.server.common.Result;
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

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return Result.error("消息不能为空");
        }
        Long userId = getCurrentUserId();
        try {
            UserContext.set(userId);
            String reply = aiOrderAssistant.chat(userId, request.getMessage());
            return Result.success(reply);
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
