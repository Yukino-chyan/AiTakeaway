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


-- ========================
-- 商家表（Merchant 模块）
-- ========================
CREATE TABLE IF NOT EXISTS `merchant` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '商家ID',
    `user_id`         BIGINT        NOT NULL COMMENT '关联的用户ID（商家账号）',
    `name`            VARCHAR(64)   NOT NULL COMMENT '店铺名称',
    `avatar`          VARCHAR(255)            COMMENT '店铺头像URL',
    `phone`           VARCHAR(20)             COMMENT '联系电话',
    `address`         VARCHAR(255)            COMMENT '店铺地址',
    `business_hours`  VARCHAR(64)  DEFAULT '09:00-21:00' COMMENT '营业时间',
    `delivery_range`  INT          DEFAULT 3   COMMENT '配送范围（km）',
    `delivery_fee`    DECIMAL(10,2) DEFAULT 3.00 COMMENT '配送费（元）',
    `description`     TEXT                      COMMENT '店铺描述',
    `status`          TINYINT      DEFAULT 1   COMMENT '状态：0休息中 1营业中',
    `deleted`         TINYINT      DEFAULT 0   COMMENT '逻辑删除：0正常 1已删除',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_name` (`name`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';