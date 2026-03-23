package com.aics.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * PGVector 数据源配置
 * 独立的 PostgreSQL 连接，用于向量存储
 */
@Configuration
public class PgVectorConfig {

    @Value("${pgvector.url}")
    private String url;

    @Value("${pgvector.username}")
    private String username;

    @Value("${pgvector.password}")
    private String password;

    // 移除 @Bean 注解，防止该数据源覆盖 Spring Boot 默认的 MySQL 主数据源
    public DataSource pgDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public JdbcTemplate pgJdbcTemplate() {
        return new JdbcTemplate(pgDataSource());
    }
}
