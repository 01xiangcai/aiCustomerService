package com.aics.server.controller;

import com.aics.common.entity.Message;
import com.aics.common.entity.Session;
import com.aics.common.result.PageResult;
import com.aics.common.result.R;
import com.aics.server.mapper.MessageMapper;
import com.aics.server.mapper.SessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端对话记录接口（需要登录）
 */
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class ChatRecordController {

    private final SessionMapper sessionMapper;
    private final MessageMapper messageMapper;

    /**
     * 查询某个应用的会话列表（分页）
     */
    @GetMapping("/{appId}/sessions")
    public R<PageResult<Session>> listSessions(
            @PathVariable("appId") Long appId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Authentication auth) {

        IPage<Session> iPage = sessionMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getAppId, appId)
                        .orderByDesc(Session::getLastActiveAt));

        PageResult<Session> result = new PageResult<>(
                iPage.getTotal(),
                iPage.getRecords(),
                iPage.getCurrent(),
                iPage.getSize());
        return R.ok(result);
    }

    /**
     * 查询某个会话的消息记录
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public R<List<Message>> listMessages(
            @PathVariable("sessionId") Long sessionId,
            Authentication auth) {

        List<Message> messages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getSessionId, sessionId)
                        .orderByAsc(Message::getCreatedAt));

        return R.ok(messages);
    }
}
