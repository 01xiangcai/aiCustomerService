package com.aics.server.controller;

import com.aics.common.entity.Message;
import com.aics.common.result.R;
import com.aics.server.dto.request.ChatRequest;
import com.aics.server.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 开放对话接口（无需登录，通过 AppKey 鉴权）
 * 供 Starter SDK 和 Widget 调用
 */
@RestController
@RequestMapping("/open/chat")
@RequiredArgsConstructor
public class OpenChatController {

    private final ChatService chatService;

    /**
     * 普通对话（非流式）
     */
    @PostMapping("/{appKey}/message")
    public R<String> chat(@PathVariable("appKey") String appKey,
            @Valid @RequestBody ChatRequest request) {
        String reply = chatService.chat(
                appKey, request.getSessionId(),
                request.getMessage(), request.getUserId());
        return R.ok(reply);
    }

    /**
     * 流式对话（SSE）
     */
    @PostMapping(value = "/{appKey}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@PathVariable("appKey") String appKey,
            @Valid @RequestBody ChatRequest request) {
        return chatService.streamChat(
                appKey, request.getSessionId(),
                request.getMessage(), request.getUserId());
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/{appKey}/history")
    public R<List<Message>> getHistory(@PathVariable("appKey") String appKey,
            @RequestParam("sessionId") String sessionId,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return R.ok(chatService.getHistory(appKey, sessionId, limit));
    }
}
