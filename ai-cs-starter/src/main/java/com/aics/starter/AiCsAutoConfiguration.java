package com.aics.starter;

import com.aics.starter.controller.AiCsEndpointController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * AI 客服 Starter 自动配置类
 * 当配置 ai-cs.enabled=true（默认）时自动生效
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "ai-cs", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AiCsProperties.class)
public class AiCsAutoConfiguration {

    /**
     * 创建 WebClient（HTTP 客户端）
     */
    @Bean
    @ConditionalOnMissingBean
    public WebClient aiCsWebClient(AiCsProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(properties.getReadTimeout()));

        return WebClient.builder()
                .baseUrl(properties.getServerUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("X-App-Key", properties.getAppKey())
                .defaultHeader("X-App-Secret", properties.getAppSecret())
                .build();
    }

    /**
     * 创建 AI 客服客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public AiCsClient aiCsClient(AiCsProperties properties, WebClient aiCsWebClient) {
        log.info("✅ AI 客服 Starter 初始化完成，AppKey: {}..., 服务地址: {}",
                properties.getAppKey() != null
                        ? properties.getAppKey().substring(0, Math.min(10, properties.getAppKey().length()))
                        : "未配置",
                properties.getServerUrl());
        return new AiCsClient(properties, aiCsWebClient);
    }

    /**
     * 自动注册对话接口
     */
    @Bean
    @ConditionalOnMissingBean
    public AiCsEndpointController aiCsEndpointController(AiCsClient aiCsClient) {
        return new AiCsEndpointController(aiCsClient);
    }
}
