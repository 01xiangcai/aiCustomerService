package com.aics.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息表
 */
@Data
@TableName("acs_message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属会话 */
    private Long sessionId;

    /** 所属应用 */
    private Long appId;

    /** 角色：user / assistant / system */
    private String role;

    /** 消息内容 */
    @TableField("`content`")
    private String content;

    /** 消耗 Token 数 */
    private Integer tokensUsed;

    /** 来源：rag / model / fallback */
    private String source;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
