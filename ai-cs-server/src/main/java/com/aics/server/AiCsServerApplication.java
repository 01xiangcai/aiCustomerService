package com.aics.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI 客服中间件 SaaS 后端启动类
 */
@SpringBootApplication
@MapperScan("com.aics.server.mapper")
@EnableAsync
public class AiCsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCsServerApplication.class, args);
    }
}
