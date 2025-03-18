package com.wangguangwu.distributedlockredis.lock.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试基于 RedLock（RedissonMultiLock）实现的分布式锁
 *
 * @author wangguangwu
 */
@SpringBootTest
@DisplayName("RedLock Distributed Lock Tests")
public class RedLockDistributedLockImplTest {

    @Autowired
    private RedLockDistributedLockImpl redLockDistributedLockImpl;

    @Test
    @DisplayName("测试 RedLockDistributedLockImpl 的加锁功能")
    void testLock() {
        boolean locked = redLockDistributedLockImpl.lock();
        assertTrue(locked, "应该成功获取 RedLock 分布式锁");
        redLockDistributedLockImpl.unlock();
    }

    @Test
    @DisplayName("测试 RedLockDistributedLockImpl 的释放锁功能")
    void testUnlock() {
        boolean locked = redLockDistributedLockImpl.lock();
        assertTrue(locked, "应该成功获取 RedLock 分布式锁");
        redLockDistributedLockImpl.unlock();
        boolean lockedAgain = redLockDistributedLockImpl.lock();
        assertTrue(lockedAgain, "释放后应能再次获取 RedLock 分布式锁");
        redLockDistributedLockImpl.unlock();
    }
}