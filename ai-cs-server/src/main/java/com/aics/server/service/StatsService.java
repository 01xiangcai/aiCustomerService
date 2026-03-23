package com.aics.server.service;

import com.aics.server.mapper.MessageMapper;
import com.aics.server.mapper.SessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aics.common.entity.Message;
import com.aics.common.entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计分析服务
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final MessageMapper messageMapper;
    private final SessionMapper sessionMapper;

    /**
     * 获取应用数据概览
     *
     * @param appId 应用 ID
     * @return 数据概览
     */
    public Map<String, Object> getOverview(Long appId) {
        Map<String, Object> overview = new HashMap<>();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 今日对话数
        Long todaySessions = sessionMapper.selectCount(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getAppId, appId)
                        .ge(Session::getCreatedAt, todayStart));
        overview.put("todaySessions", todaySessions);

        // 总对话数
        Long totalSessions = sessionMapper.selectCount(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getAppId, appId));
        overview.put("totalSessions", totalSessions);

        // 今日消息数
        Long todayMessages = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getAppId, appId)
                        .ge(Message::getCreatedAt, todayStart));
        overview.put("todayMessages", todayMessages);

        // 总消息数
        Long totalMessages = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getAppId, appId));
        overview.put("totalMessages", totalMessages);

        // 兜底回复次数（AI 无法回答）
        Long fallbackCount = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getAppId, appId)
                        .eq(Message::getSource, "fallback"));
        overview.put("fallbackCount", fallbackCount);

        // AI 解答率
        Long aiAnswered = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getAppId, appId)
                        .eq(Message::getRole, "assistant"));
        double resolveRate = aiAnswered > 0
                ? (double) (aiAnswered - fallbackCount) / aiAnswered * 100
                : 0;
        overview.put("resolveRate", Math.round(resolveRate * 10) / 10.0);

        return overview;
    }

    /**
     * 获取最近 7 天消息量趋势
     */
    public List<Map<String, Object>> getMessageTrend(Long appId, int days) {
        List<Map<String, Object>> trend = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Long count = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getAppId, appId)
                            .between(Message::getCreatedAt, start, end));

            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toString());
            point.put("count", count);
            trend.add(point);
        }

        return trend;
    }
}
