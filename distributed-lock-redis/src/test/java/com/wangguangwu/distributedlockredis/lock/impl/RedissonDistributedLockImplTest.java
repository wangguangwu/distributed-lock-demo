package com.wangguangwu.distributedlockredis.lock.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wangguangwu
 */
/**
 * 测试基于 Redisson 单实例实现的分布式锁
 */
@SpringBootTest
@DisplayName("Redisson Distributed Lock Tests")
public class RedissonDistributedLockImplTest {

    @Autowired
    private RedissonDistributedLockImpl redissonDistributedLockImpl;

    @Test
    @DisplayName("测试 RedissonDistributedLockImpl 的加锁功能")
    void testLock() {
        boolean locked = redissonDistributedLockImpl.lock();
        assertTrue(locked, "应该成功获取 Redisson 分布式锁");
        redissonDistributedLockImpl.unlock();
    }

    @Test
    @DisplayName("测试 RedissonDistributedLockImpl 的释放锁功能")
    void testUnlock() {
        boolean locked = redissonDistributedLockImpl.lock();
        assertTrue(locked, "应该成功获取 Redisson 分布式锁");
        redissonDistributedLockImpl.unlock();
        boolean lockedAgain = redissonDistributedLockImpl.lock();
        assertTrue(lockedAgain, "释放后应能再次获取 Redisson 分布式锁");
        redissonDistributedLockImpl.unlock();
    }
}