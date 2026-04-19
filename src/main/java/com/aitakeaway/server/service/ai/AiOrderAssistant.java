package com.aitakeaway.server.service.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

public interface AiOrderAssistant {

    String chat(@MemoryId Long userId, @UserMessage String message);
}
