package com.aics.server.controller;

import com.aics.common.entity.App;
import com.aics.common.result.PageResult;
import com.aics.common.result.R;
import com.aics.server.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 应用管理接口（需要登录）
 */
@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    /**
     * 创建应用
     */
    @PostMapping
    public R<App> createApp(@RequestBody App app, Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        App created = appService.createApp(tenantId, app.getAppName());
        return R.ok(created);
    }

    /**
     * 查询应用列表
     */
    @GetMapping
    public R<PageResult<App>> listApps(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        return R.ok(appService.listApps(tenantId, page, size));
    }

    /**
     * 查询应用详情
     */
    @GetMapping("/{id}")
    public R<App> getApp(@PathVariable("id") Long id, Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        return R.ok(appService.getApp(tenantId, id));
    }

    /**
     * 更新应用配置
     */
    @PutMapping("/{id}")
    public R<App> updateApp(@PathVariable("id") Long id, @RequestBody App app, Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        return R.ok(appService.updateApp(tenantId, id, app));
    }

    /**
     * 重置 AppSecret
     */
    @PostMapping("/{id}/reset-secret")
    public R<App> resetSecret(@PathVariable("id") Long id, Authentication auth) {
        Long tenantId = (Long) auth.getPrincipal();
        return R.ok(appService.resetAppSecret(tenantId, id));
    }
}
