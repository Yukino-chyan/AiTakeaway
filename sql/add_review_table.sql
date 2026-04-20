-- 评价表迁移脚本
-- 在已有数据库上执行即可，不影响其他表

USE ai_takeaway;

CREATE TABLE IF NOT EXISTS `review` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评价用户ID',
    `merchant_id` BIGINT       NOT NULL COMMENT '商家ID',
    `order_id`    BIGINT       NOT NULL COMMENT '关联订单ID（每订单只能评一次）',
    `rating`      TINYINT      NOT NULL COMMENT '评分 1-5',
    `content`     VARCHAR(500)          COMMENT '评价内容',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_review` (`order_id`, `deleted`),
    KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单评价表';
