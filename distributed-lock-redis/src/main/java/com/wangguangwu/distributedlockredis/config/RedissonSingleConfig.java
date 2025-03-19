package com.wangguangwu.distributedlockredis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangguangwu
 */
@Configuration
public class RedissonSingleConfig {

    @Value("${redisson.address}")
    private String redissonAddress;

    @Bean(name = "redissonSingleClient")
    public RedissonClient redissonSingleClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(redissonAddress);
        return Redisson.create(config);
    }
}