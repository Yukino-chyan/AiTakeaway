package com.aitakeaway.server.config;

import com.aitakeaway.server.service.ai.AiOrderAssistant;
import com.aitakeaway.server.service.ai.AiResponseFormatter;
import com.aitakeaway.server.service.ai.IntentRouter;
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
    private final Map<Long, ChatMemory> memoryStore = new ConcurrentHashMap<>();

    public void clearUserMemory(Long userId) {
        memoryStore.remove(userId);
    }

    @Bean
    public IntentRouter intentRouter() {
        return AiServices.builder(IntentRouter.class)
                .chatLanguageModel(chatModel)
                .build();
    }

    @Bean
    public AiResponseFormatter aiResponseFormatter() {
        return AiServices.builder(AiResponseFormatter.class)
                .chatLanguageModel(chatModel)
                .chatMemoryProvider(userId ->
                        memoryStore.computeIfAbsent((Long) userId,
                                id -> MessageWindowChatMemory.withMaxMessages(20)))
                .build();
    }

    @Bean
    public AiOrderAssistant aiOrderAssistant() {
        return AiServices.builder(AiOrderAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemoryProvider(userId ->
                        memoryStore.computeIfAbsent((Long) userId,
                                id -> MessageWindowChatMemory.withMaxMessages(20)))
                .tools(tools)
                .build();
    }
}
