package com.example.gooha.miniproject.config;

import com.example.gooha.miniproject.sharding.Snowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    //2024년 1월 1일 00:00:00 UTC
    private static final long CUSTOM_EPOCH = 1704067200000L;

    @Value("${snowflake.node-id:0}")
    private long nodeId;

    @Bean
    public Snowflake snowflake() {

        return new Snowflake(nodeId, CUSTOM_EPOCH);
    }

}
