package com.wangguangwu.distributedlockredis.lock.impl;

import com.wangguangwu.distributedlockredis.lock.AbstractDistributedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 单实例实现的分布式锁，使用 RLock 实现。
 *
 * @author wangguangwu
 */
@Component
public class RedissonDistributedLockImpl extends AbstractDistributedLock {

    private final RedissonClient redissonClient;

    public RedissonDistributedLockImpl(@Qualifier("redissonSingleClient") RedissonClient redissonClient) {
        super("redissonLock");
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean lock() {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试加锁：最多等待 5 秒，上锁后 10 秒自动释放
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                System.out.println("[Redisson] 获取锁成功: " + lockKey);
                return true;
            } else {
                System.out.println("[Redisson] 获取锁失败: " + lockKey);
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
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            System.out.println("[Redisson] 释放锁: " + lockKey);
        }
    }
}