package com.aitakeaway.server.config;

import com.aitakeaway.server.service.ai.AiOrderAssistant;
import com.aitakeaway.server.service.ai.OrderAssistantTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final OpenAiChatModel chatModel;
    private final OrderAssistantTools tools;

    // 按用户ID隔离的对话记忆，服务重启后清零（课程项目可接受）
    private final Map<Long, ChatMemory> memoryStore = new ConcurrentHashMap<>();

    @Bean
    public AiOrderAssistant aiOrderAssistant() {
        return AiServices.builder(AiOrderAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemoryProvider(userId ->
                        memoryStore.computeIfAbsent((Long) userId,
                                id -> MessageWindowChatMemory.withMaxMessages(10)))
                .tools(tools)
                .systemMessageProvider(userId ->
                        "你是一个外卖平台的AI点餐助手。当前用户ID是 " + userId + "。\n" +
                        "你的职责：\n" +
                        "1. 根据用户描述的口味、预算、偏好，调用工具查询菜品并推荐\n" +
                        "2. 用户确认后，调用工具将菜品加入购物车\n" +
                        "3. 推荐时说明菜名、价格和推荐理由，每次最多推荐3道\n" +
                        "4. 查询菜品时必须知道商家ID，如果用户没有提供，请询问\n" +
                        "5. 加入购物车时使用当前用户ID(" + userId + ")和菜品ID\n" +
                        "请用简洁友好的中文回复。")
                .build();
    }
}
