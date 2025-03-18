package com.wangguangwu.distributedlockredis.lock.impl;

import com.wangguangwu.distributedlockredis.lock.AbstractDistributedLock;
import org.redisson.RedissonMultiLock;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于 RedissonMultiLock 实现 RedLock 算法的分布式锁。
 * 此实现依赖于 RedissonRedLockConfig 中配置的 5 个 Redis 实例。
 *
 * @author wangguangwu
 */
@Component
public class RedLockDistributedLockImpl extends AbstractDistributedLock {

    private final RedissonMultiLock redissonMultiLock;

    public RedLockDistributedLockImpl(RedissonMultiLock redissonMultiLock) {
        // 注意：此处锁标识与配置中保持一致
        super("myLock");
        // redissonMultiLock 已在配置中组合了多个单节点锁，统一使用锁名 "myLock"
        this.redissonMultiLock = redissonMultiLock;
    }

    @Override
    public boolean lock() {
        try {
            if (redissonMultiLock.tryLock(5, 10, TimeUnit.SECONDS)) {
                System.out.println("[RedLock] 获取锁成功: " + lockKey);
                return true;
            } else {
                System.out.println("[RedLock] 获取锁失败: " + lockKey);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        if (redissonMultiLock.isHeldByCurrentThread()) {
            redissonMultiLock.unlock();
            System.out.println("[RedLock] 释放锁: " + lockKey);
        }
    }

    @Override
    public boolean isLockHeldByCurrentThread() {
        throw new UnsupportedOperationException();
    }
}
