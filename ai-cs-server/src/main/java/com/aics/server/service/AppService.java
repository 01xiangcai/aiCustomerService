package com.aics.server.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.aics.common.entity.App;
import com.aics.common.exception.BizException;
import com.aics.common.result.PageResult;
import com.aics.server.mapper.AppMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 应用（客服机器人）管理服务
 */
@Service
@RequiredArgsConstructor
public class AppService {

    private final AppMapper appMapper;

    /**
     * 创建应用
     *
     * @param tenantId 租户 ID
     * @param appName  应用名称
     * @return 创建的应用
     */
    public App createApp(Long tenantId, String appName) {
        App app = new App();
        app.setTenantId(tenantId);
        app.setAppName(appName);
        // 生成 AppKey 和 AppSecret
        app.setAppKey("ak_" + RandomUtil.randomString(24));
        app.setAppSecret("sk_" + RandomUtil.randomString(32));
        app.setWelcomeMsg("你好！我是 AI 智能客服，请问有什么可以帮助你的？");
        app.setFallbackMsg("抱歉，我暂时无法回答这个问题，建议联系人工客服。");
        app.setModelType("deepseek");
        app.setThemeColor("#4f46e5");
        app.setStatus(1);
        appMapper.insert(app);
        return app;
    }

    /**
     * 分页查询租户下的应用列表
     */
    public PageResult<App> listApps(Long tenantId, int page, int size) {
        Page<App> pageParam = new Page<>(page, size);
        Page<App> result = appMapper.selectPage(pageParam,
                new LambdaQueryWrapper<App>()
                        .eq(App::getTenantId, tenantId)
                        .orderByDesc(App::getCreatedAt));
        return new PageResult<>(result.getTotal(), result.getRecords(),
                result.getCurrent(), result.getSize());
    }

    /**
     * 查询应用详情
     */
    public App getApp(Long tenantId, Long appId) {
        App app = appMapper.selectById(appId);
        if (app == null || !app.getTenantId().equals(tenantId)) {
            throw BizException.notFound("应用不存在");
        }
        return app;
    }

    /**
     * 更新应用配置
     */
    public App updateApp(Long tenantId, Long appId, App updateData) {
        App app = getApp(tenantId, appId);
        if (updateData.getAppName() != null)
            app.setAppName(updateData.getAppName());
        if (updateData.getWelcomeMsg() != null)
            app.setWelcomeMsg(updateData.getWelcomeMsg());
        if (updateData.getFallbackMsg() != null)
            app.setFallbackMsg(updateData.getFallbackMsg());
        if (updateData.getModelType() != null)
            app.setModelType(updateData.getModelType());
        if (updateData.getThemeColor() != null)
            app.setThemeColor(updateData.getThemeColor());
        appMapper.updateById(app);
        return app;
    }

    /**
     * 重置 AppSecret
     */
    public App resetAppSecret(Long tenantId, Long appId) {
        App app = getApp(tenantId, appId);
        app.setAppSecret("sk_" + RandomUtil.randomString(32));
        appMapper.updateById(app);
        return app;
    }

    /**
     * 根据 AppKey 查找应用（供开放接口使用）
     */
    public App getAppByKey(String appKey) {
        App app = appMapper.selectOne(
                new LambdaQueryWrapper<App>()
                        .eq(App::getAppKey, appKey)
                        .eq(App::getStatus, 1));
        if (app == null) {
            throw BizException.unauthorized("无效的 AppKey");
        }
        return app;
    }
}
