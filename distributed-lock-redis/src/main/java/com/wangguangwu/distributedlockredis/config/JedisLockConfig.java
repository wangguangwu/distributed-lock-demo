package com.wangguangwu.distributedlockredis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

/**
 * @author wangguangwu
 */
@Configuration
public class JedisLockConfig {

    @Value("${jedis.host}")
    private String jedisHost;

    @Value("${jedis.port}")
    private int jedisPort;

    @Bean
    public JedisPool jedisPool() {
        return new JedisPool(jedisHost, jedisPort);
    }
}