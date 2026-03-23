-- =============================================
-- AI 客服中间件 · 数据库初始化脚本
-- 数据库: ai_customer_service (MySQL 8)
-- =============================================

CREATE DATABASE IF NOT EXISTS `ai_customer_service` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ai_customer_service`;

-- 租户表
CREATE TABLE IF NOT EXISTS `acs_tenant` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `tenant_name` VARCHAR(100) NOT NULL COMMENT '租户名称',
    `email` VARCHAR(100) NOT NULL COMMENT '登录邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 应用表（客服机器人）
CREATE TABLE IF NOT EXISTS `acs_app` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属租户',
    `app_name` VARCHAR(100) NOT NULL COMMENT '应用名称',
    `app_key` VARCHAR(50) NOT NULL COMMENT 'AppKey（公开标识）',
    `app_secret` VARCHAR(100) NOT NULL COMMENT 'AppSecret（签名密钥）',
    `welcome_msg` VARCHAR(500) DEFAULT NULL COMMENT '欢迎语',
    `fallback_msg` VARCHAR(500) DEFAULT NULL COMMENT '兜底回复',
    `model_type` VARCHAR(50) NOT NULL DEFAULT 'deepseek' COMMENT '模型类型',
    `theme_color` VARCHAR(20) DEFAULT '#4f46e5' COMMENT '主题色',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_key` (`app_key`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用表';

-- 知识库文档表
CREATE TABLE IF NOT EXISTS `acs_document` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `app_id` BIGINT NOT NULL COMMENT '所属应用',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_type` VARCHAR(20) NOT NULL COMMENT '文件类型',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '存储路径',
    `parse_status` TINYINT NOT NULL DEFAULT 0 COMMENT '解析状态：0-待解析，1-解析中，2-已完成，3-失败',
    `chunk_count` INT NOT NULL DEFAULT 0 COMMENT '分块数量',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '解析失败原因',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 会话表
CREATE TABLE IF NOT EXISTS `acs_session` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `app_id` BIGINT NOT NULL COMMENT '所属应用',
    `session_key` VARCHAR(100) NOT NULL COMMENT '会话唯一标识',
    `user_identifier` VARCHAR(100) DEFAULT NULL COMMENT '用户标识',
    `user_ip` VARCHAR(50) DEFAULT NULL COMMENT '用户IP',
    `message_count` INT NOT NULL DEFAULT 0 COMMENT '消息总数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_active_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_session` (`app_id`, `session_key`),
    KEY `idx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS `acs_message` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '所属会话',
    `app_id` BIGINT NOT NULL COMMENT '所属应用',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant/system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `tokens_used` INT DEFAULT NULL COMMENT '消耗Token数',
    `source` VARCHAR(20) DEFAULT NULL COMMENT '来源：rag/model/fallback',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 套餐表
CREATE TABLE IF NOT EXISTS `acs_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `plan_name` VARCHAR(50) NOT NULL COMMENT '套餐名称',
    `monthly_msgs` INT NOT NULL DEFAULT 0 COMMENT '每月消息数',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格（元/月）',
    `features_json` JSON DEFAULT NULL COMMENT '功能特性JSON',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- 租户套餐表
CREATE TABLE IF NOT EXISTS `acs_tenant_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `plan_id` BIGINT NOT NULL COMMENT '套餐ID',
    `expire_at` DATETIME DEFAULT NULL COMMENT '过期时间',
    `msg_used` INT NOT NULL DEFAULT 0 COMMENT '已使用消息数',
    `msg_limit` INT NOT NULL DEFAULT 1000 COMMENT '消息上限',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户套餐表';

-- 初始化免费套餐
INSERT INTO `acs_plan` (`plan_name`, `monthly_msgs`, `price`, `features_json`) VALUES
('免费版', 1000, 0.00, '{"knowledge_count": 1, "max_file_size": "10MB"}'),
('专业版', 50000, 99.00, '{"knowledge_count": 10, "max_file_size": "50MB", "human_support": true}'),
('企业版', 500000, 999.00, '{"knowledge_count": 100, "max_file_size": "200MB", "human_support": true, "custom_model": true}');
