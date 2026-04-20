package com.aitakeaway.server.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IntentRouter {

    @SystemMessage("""
            外卖平台意图分类器。只返回以下格式之一，禁止输出其他任何内容：

            SEARCH|搜索词   用户想找菜、得到推荐、询问有没有某类菜时使用
                           搜索词规则：口味/菜名直接提取；场景描述推断食物词；实在不确定则留空
            OTHER           加购/查订单/闲聊等非搜索操作

            示例：
            "我想吃辣的"           → SEARCH|辣
            "有什么酸辣菜推荐"      → SEARCH|酸辣
            "我最近在健身"          → SEARCH|鸡胸
            "今天好冷"             → SEARCH|汤
            "随便推荐点吃的"        → SEARCH|
            "帮我加1份黄焖鸡"       → OTHER
            "把菜品ID 23 加入购物车" → OTHER
            "你好"                 → OTHER
            """)
    String classify(@UserMessage String message);
}
