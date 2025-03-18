package com.wangguangwu.distributedlockredis.config;

import com.wangguangwu.distributedlockredis.properties.RedissonRedLockProperties;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangguangwu
 */
@Configuration
@RequiredArgsConstructor
public class RedissonRedLockConfig {

    private final RedissonRedLockProperties redissonRedLockProperties;

    @Bean(name = "redissonClientsForRedLock")
    public List<RedissonClient> redissonClientsForRedLock() {
        List<RedissonClient> clients = new ArrayList<>();
        for (String address : redissonRedLockProperties.getAddresses()) {
            Config config = new Config();
            config.useSingleServer().setAddress(address);
            clients.add(Redisson.create(config));
        }
        return clients;
    }

    @Bean(name = "redissonMultiLock")
    public RedissonMultiLock redissonMultiLock(List<RedissonClient> redissonClientsForRedLock) {
        List<RLock> locks = new ArrayList<>();
        // 此处所有客户端都使用同一把锁名称 "myLock"，以实现 RedLock 效果
        for (RedissonClient client : redissonClientsForRedLock) {
            locks.add(client.getLock("myLock"));
        }
        return new RedissonMultiLock(locks.toArray(new RLock[0]));
    }
}