package com.aics.server.service;

import com.aics.common.entity.App;
import com.aics.common.entity.Message;
import com.aics.common.entity.Session;
import com.aics.server.mapper.MessageMapper;
import com.aics.server.mapper.SessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话核心服务
 * 负责对话管理、消息处理、AI 调用和流式响应
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel chatModel;
    private final SessionMapper sessionMapper;
    private final MessageMapper messageMapper;
    private final AppService appService;
    private final VectorStoreService vectorStoreService;

    /**
     * 系统提示词模板
     */
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是 %s 的 AI 智能客服助手。
            %s
            请遵循以下规则：
            1. 用友善、专业的语气回复用户问题
            2. 如果你不确定答案，请如实告知用户,不要编造内容
            3. 回复要简洁明了，避免不必要的冗长
            4. 支持使用 Markdown 格式回复
            """;

    /** RAG 知识库上下文模板 */
    private static final String RAG_CONTEXT_TEMPLATE = """

            以下是与用户问题相关的知识库内容，请优先基于这些内容回答：
            ---
            %s
            ---
            如果知识库内容不足以回答用户问题，可以结合你的通用知识补充，但需要说明哪些是来自知识库的信息。
            """;

    /**
     * 普通对话（非流式）
     *
     * @param appKey     应用 Key
     * @param sessionKey 会话标识
     * @param content    用户消息
     * @param userId     用户标识
     * @return AI 回复内容
     */
    public String chat(String appKey, String sessionKey, String content, String userId) {
        App app = appService.getAppByKey(appKey);

        // 获取或创建会话
        Session session = getOrCreateSession(app.getId(), sessionKey, userId);

        // 保存用户消息
        saveMessage(session.getId(), app.getId(), "user", content, "user");

        // 构建提示词
        Prompt prompt = buildPrompt(app, session.getId(), content);

        // 调用 AI 模型
        try {
            ChatResponse response = chatModel.call(prompt);
            String reply = response.getResult().getOutput().getText();

            // 保存 AI 回复
            saveMessage(session.getId(), app.getId(), "assistant", reply, "model");

            // 更新会话活跃时间
            updateSessionActivity(session);

            return reply;
        } catch (Exception e) {
            log.error("AI 模型调用失败: type={}, message={}", e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                log.error("AI 模型异常根因: type={}, message={}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            log.error("AI 模型完整堆栈:", e);
            try {
                java.nio.file.Files.writeString(java.nio.file.Path.of("error.log"), 
                    "Error: " + e.getMessage() + "\n" + 
                    java.util.Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(java.util.stream.Collectors.joining("\n")));
            } catch (Exception ignored) {}
            // 返回兜底回复
            String fallback = app.getFallbackMsg();
            saveMessage(session.getId(), app.getId(), "assistant", fallback, "fallback");
            return fallback;
        }
    }

    /**
     * 流式对话（SSE）
     *
     * @param appKey     应用 Key
     * @param sessionKey 会话标识
     * @param content    用户消息
     * @param userId     用户标识
     * @return 流式文本片段
     */
    public Flux<String> streamChat(String appKey, String sessionKey, String content, String userId) {
        App app = appService.getAppByKey(appKey);

        Session session = getOrCreateSession(app.getId(), sessionKey, userId);
        saveMessage(session.getId(), app.getId(), "user", content, "user");

        Prompt prompt = buildPrompt(app, session.getId(), content);

        StringBuilder fullReply = new StringBuilder();

        return chatModel.stream(prompt)
                .map(response -> {
                    String text = response.getResult() != null
                            ? response.getResult().getOutput().getText()
                            : "";
                    if (text != null) {
                        fullReply.append(text);
                    }
                    return text != null ? text : "";
                })
                .doOnComplete(() -> {
                    // 流结束后保存完整回复
                    saveMessage(session.getId(), app.getId(), "assistant",
                            fullReply.toString(), "model");
                    updateSessionActivity(session);
                })
                .doOnError(e -> {
                    log.error("流式对话异常", e);
                    saveMessage(session.getId(), app.getId(), "assistant",
                            app.getFallbackMsg(), "fallback");
                });
    }

    /**
     * 获取会话历史消息
     */
    public List<Message> getHistory(String appKey, String sessionKey, int limit) {
        App app = appService.getAppByKey(appKey);

        Session session = sessionMapper.selectOne(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getAppId, app.getId())
                        .eq(Session::getSessionKey, sessionKey));
        if (session == null) {
            return List.of();
        }

        return messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getSessionId, session.getId())
                        .orderByAsc(Message::getCreatedAt)
                        .last("LIMIT " + limit));
    }

    /**
     * 构建 AI 提示词（含历史上下文）
     */
    private Prompt buildPrompt(App app, Long sessionId, String userContent) {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();

        // 系统提示词
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE,
                app.getAppName(),
                app.getWelcomeMsg() != null ? "产品介绍: " + app.getWelcomeMsg() : "");

        // RAG 知识库检索：根据用户问题检索相关文档分块
        List<String> ragResults = vectorStoreService.searchSimilar(app.getId(), userContent, 3, 0.7);
        if (!ragResults.isEmpty()) {
            String ragContext = String.join("\n\n", ragResults);
            systemPrompt += String.format(RAG_CONTEXT_TEMPLATE, ragContext);
            log.info("RAG 命中 {} 条相关文档, appId={}", ragResults.size(), app.getId());
        }

        messages.add(new SystemMessage(systemPrompt));

        // 加载最近的历史消息（最多 10 条）作为上下文
        List<Message> history = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getSessionId, sessionId)
                        .orderByDesc(Message::getCreatedAt)
                        .last("LIMIT 10"));

        // 反转为按时间正序
        for (int i = history.size() - 1; i >= 0; i--) {
            Message msg = history.get(i);
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // 当前用户消息
        messages.add(new UserMessage(userContent));

        return new Prompt(messages);
    }

    /**
     * 获取或创建会话
     */
    private Session getOrCreateSession(Long appId, String sessionKey, String userId) {
        Session session = sessionMapper.selectOne(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getAppId, appId)
                        .eq(Session::getSessionKey, sessionKey));

        if (session == null) {
            session = new Session();
            session.setAppId(appId);
            session.setSessionKey(sessionKey);
            session.setUserIdentifier(userId);
            session.setMessageCount(0);
            session.setLastActiveAt(LocalDateTime.now());
            sessionMapper.insert(session);
        }

        return session;
    }

    /**
     * 保存消息
     */
    private void saveMessage(Long sessionId, Long appId, String role, String content, String source) {
        Message message = new Message();
        message.setSessionId(sessionId);
        message.setAppId(appId);
        message.setRole(role);
        message.setContent(content);
        message.setSource(source);
        messageMapper.insert(message);
    }

    /**
     * 更新会话活跃时间和消息计数
     */
    private void updateSessionActivity(Session session) {
        session.setLastActiveAt(LocalDateTime.now());
        session.setMessageCount(session.getMessageCount() + 2); // 用户消息 + AI回复
        sessionMapper.updateById(session);
    }
}
