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
        // 用局部变量而非字段，避免 Spring 代理干扰
        Map<Long, ChatMemory> store = new ConcurrentHashMap<>();
        return AiServices.builder(AiOrderAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemoryProvider(userId ->
                        store.computeIfAbsent((Long) userId,
                                id -> MessageWindowChatMemory.withMaxMessages(10)))
                .tools(tools)
                .systemMessageProvider(userId ->
                        "你是一个外卖平台的AI点餐助手。\n" +
                        "你的职责：\n" +
                        "1. 用户描述口味/偏好但未指定商家时，调用 searchDishesGlobal 跨全平台搜索菜品\n" +
                        "2. 用户已指定商家时，调用 queryDishes 查询该商家菜品\n" +
                        "3. 推荐时说明菜名、价格、所属商家和推荐理由，每次最多推荐3道\n" +
                        "4. 用户确认后，调用 addToCart 将菜品加入购物车（无需传用户ID，系统自动处理）\n" +
                        "请用简洁友好的中文回复。")
                .build();
    }
}
