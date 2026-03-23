package com.aics.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 对话请求 DTO
 */
@Data
public class ChatRequest {

    @NotBlank(message = "消息内容不能为空")
    private String message;

    /** 会话标识（客户端维护） */
    private String sessionId;

    /** 用户标识（可选） */
    private String userId;
}
