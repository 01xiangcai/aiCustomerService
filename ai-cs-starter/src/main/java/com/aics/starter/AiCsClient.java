package com.aics.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 客服 HTTP 客户端
 * 封装与 SaaS 后端的所有通信
 */
@Slf4j
@RequiredArgsConstructor
public class AiCsClient {

    private final AiCsProperties properties;
    private final WebClient webClient;

    /**
     * 普通对话（非流式）
     *
     * @param message   用户消息
     * @param sessionId 会话 ID
     * @param userId    用户标识
     * @return AI 回复
     */
    public String chat(String message, String sessionId, String userId) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        body.put("sessionId", sessionId);
        body.put("userId", userId);

        String url = String.format("/open/chat/%s/message", properties.getAppKey());

        Map response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null && response.get("data") != null) {
            return response.get("data").toString();
        }
        return "服务暂时不可用";
    }

    /**
     * 流式对话（SSE）
     *
     * @param message   用户消息
     * @param sessionId 会话 ID
     * @param userId    用户标识
     * @return 流式文本
     */
    public Flux<String> streamChat(String message, String sessionId, String userId) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        body.put("sessionId", sessionId);
        body.put("userId", userId);

        String url = String.format("/open/chat/%s/stream", properties.getAppKey());

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class);
    }

    /**
     * 获取对话历史
     *
     * @param sessionId 会话 ID
     * @param limit     消息数量限制
     * @return 历史记录
     */
    public Object getHistory(String sessionId, int limit) {
        String url = String.format("/open/chat/%s/history?sessionId=%s&limit=%d",
                properties.getAppKey(), sessionId, limit);

        Map response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null) {
            return response.get("data");
        }
        return null;
    }
}
