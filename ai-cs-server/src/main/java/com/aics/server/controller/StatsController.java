package com.aics.server.controller;

import com.aics.common.result.R;
import com.aics.server.service.AppService;
import com.aics.server.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计分析接口（需要登录）
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final AppService appService;

    /**
     * 获取应用数据概览
     */
    @GetMapping("/{appId}/overview")
    public R<Map<String, Object>> getOverview(@PathVariable("appId") Long appId, Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        appService.getApp(tenantId, appId);
        return R.ok(statsService.getOverview(appId));
    }

    /**
     * 获取消息量趋势（最近 N 天）
     */
    @GetMapping("/{appId}/trend")
    public R<List<Map<String, Object>>> getMessageTrend(
            @PathVariable("appId") Long appId,
            @RequestParam(value = "days", defaultValue = "7") int days,
            Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        appService.getApp(tenantId, appId);
        return R.ok(statsService.getMessageTrend(appId, days));
    }
}
