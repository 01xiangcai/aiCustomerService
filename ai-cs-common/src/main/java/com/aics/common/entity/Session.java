package com.aics.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话表
 */
@Data
@TableName("acs_session")
public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属应用 */
    private Long appId;

    /** 会话唯一标识（客户端生成） */
    private String sessionKey;

    /** 用户标识（接入方自定义） */
    private String userIdentifier;

    /** 用户 IP */
    private String userIp;

    /** 消息总数 */
    private Integer messageCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最后活跃时间 */
    private LocalDateTime lastActiveAt;
}
