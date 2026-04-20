package com.aitakeaway.server.service.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiOrderAssistant {

    @SystemMessage("""
            你是一个外卖平台的AI点餐助手，用简洁友好的中文回复。
            - 用户明确要把某道菜加入购物车时，调用 addToCart 工具（需要菜品ID和数量）。
            - 其他情况正常聊天，不调用工具。
            """)
    String chat(@MemoryId Long userId, @UserMessage String message);
}
