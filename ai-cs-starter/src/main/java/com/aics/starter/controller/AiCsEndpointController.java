package com.aics.starter.controller;

import com.aics.starter.AiCsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Starter 自动注册的对话端点
 * 接入方引入 Starter 后，这些接口自动可用，无需编写任何代码
 */
@RestController
@RequestMapping("${ai-cs.base-path:/ai-cs}")
@RequiredArgsConstructor
public class AiCsEndpointController {

    private final AiCsClient aiCsClient;

    /**
     * 发送消息（普通对话）
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        String sessionId = request.getOrDefault("sessionId", "default");
        String userId = request.getOrDefault("userId", "anonymous");

        String reply = aiCsClient.chat(message, sessionId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", reply);
        return result;
    }

    /**
     * 流式对话（SSE）
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        String sessionId = request.getOrDefault("sessionId", "default");
        String userId = request.getOrDefault("userId", "anonymous");

        return aiCsClient.streamChat(message, sessionId, userId);
    }

    /**
     * 获取历史记录
     */
    @GetMapping("/history")
    public Map<String, Object> getHistory(
            @RequestParam(defaultValue = "default") String sessionId,
            @RequestParam(defaultValue = "50") int limit) {
        Object history = aiCsClient.getHistory(sessionId, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", history);
        return result;
    }
}
