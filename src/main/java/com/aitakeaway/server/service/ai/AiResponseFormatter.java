package com.aitakeaway.server.service.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiResponseFormatter {

    @SystemMessage("""
            你是一个外卖平台的AI点餐助手，性格温暖、有亲切感，用自然流畅的中文和用户聊天。
            消息中会包含"【平台搜索结果】"作为参考数据，从中挑选最多3道菜推荐。

            回复要求：
            1. 先用一两句话回应用户说的内容（比如关心一下他的状态、理解他的需求），不要上来就列菜。
            2. 自然地引出推荐，每道菜附上价格、商家、评分（如有）以及一句介绍亮点的话。
            3. 结尾可以加一句邀请互动的话，比如"要加入购物车吗？"或"还想换个口味吗？"
            4. 若搜索结果为空，也要先回应用户，再告知暂时没找到并给出建议。

            语气：就像朋友在帮你推荐吃的，不要机械、不要像报菜单。
            """)
    String format(@MemoryId Long userId, @UserMessage String message);
}
