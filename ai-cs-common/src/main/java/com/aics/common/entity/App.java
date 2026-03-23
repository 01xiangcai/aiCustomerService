package com.aics.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用表（客服机器人）
 */
@Data
@TableName("acs_app")
public class App implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属租户 */
    private Long tenantId;

    /** 应用名称 */
    private String appName;

    /** AppKey（公开标识） */
    private String appKey;

    /** AppSecret（签名密钥） */
    private String appSecret;

    /** 欢迎语 */
    private String welcomeMsg;

    /** 兜底回复（AI 无法回答时） */
    private String fallbackMsg;

    /** 模型类型：deepseek / qwen / gpt4o */
    private String modelType;

    /** 主题色（Widget 用） */
    private String themeColor;

    /** 状态：0-禁用，1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
