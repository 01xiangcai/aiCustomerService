package com.aics.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 客服 Starter 配置属性
 * 接入方在 application.yml 中配置
 */
@Data
@ConfigurationProperties(prefix = "ai-cs")
public class AiCsProperties {

    /** AppKey（必填） */
    private String appKey;

    /** AppSecret（必填） */
    private String appSecret;

    /** SaaS 服务地址，私有化部署时可覆盖 */
    private String serverUrl = "http://localhost:9900";

    /** 接口注册前缀 */
    private String basePath = "/ai-cs";

    /** 是否启用 */
    private boolean enabled = true;

    /** 连接超时（秒） */
    private int connectTimeout = 10;

    /** 读取超时（秒） */
    private int readTimeout = 60;
}
