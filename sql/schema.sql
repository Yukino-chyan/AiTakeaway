-- AiTakeaway 数据库初始化脚本
-- 执行前请先创建数据库：CREATE DATABASE ai_takeaway CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_takeaway;

-- ========================
-- 用户表（Auth 模块）
-- ========================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '加密密码',
    `phone`       VARCHAR(20)           COMMENT '手机号',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER' COMMENT '角色：CUSTOMER / MERCHANT / ADMIN',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
