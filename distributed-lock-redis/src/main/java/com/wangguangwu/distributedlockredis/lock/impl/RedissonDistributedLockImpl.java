package com.wangguangwu.distributedlockredis.lock.impl;

import com.wangguangwu.distributedlockredis.lock.AbstractDistributedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 单实例实现的分布式锁，使用 RLock 实现自动续期（Watchdog）机制。
 * <p>
 * 此实现通过调用 lock()（无租期参数），使 Redisson 内置的 Watchdog 自动续期机制生效，
 * 只要当前线程未调用 unlock()，锁的租期会自动延长。
 * 对于 tryLock(long, long, TimeUnit) 方法，传入租期后将禁用自动续期。
 * </p>
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

    /**
     * 使用自动续期机制加锁（Watchdog），调用 lock() 方法不指定租期，
     * Redisson 内部会默认设置一个较长初始租期（通常为30秒），并在后台自动续期。
     *
     * @return true 表示成功获取锁，否则 false
     */
    @Override
    public boolean lock() {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 不设置租期，启用 Watchdog 自动续锁
            lock.lock();
            System.out.println("[Redisson] (Watchdog) 获取锁成功: " + lockKey);
            return true;
        } catch (Exception e) {
            System.err.println("[Redisson] 加锁异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 尝试在指定等待时间内获取锁，并设置固定租期（此时不会自动续期）。
     *
     * @param waitTime  最大等待时间
     * @param leaseTime 锁的租期时间
     * @param unit      时间单位
     * @return true 表示获取锁成功，否则 false
     * @throws InterruptedException 如果线程中断
     */
    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
        if (acquired) {
            System.out.println("[Redisson] tryLock 成功: " + lockKey);
        } else {
            System.out.println("[Redisson] tryLock 失败: " + lockKey);
        }
        return acquired;
    }

    /**
     * 释放锁。Redisson 内部保证只有持锁线程才能释放锁。
     */
    @Override
    public void unlock() {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            System.out.println("[Redisson] 释放锁成功: " + lockKey);
        } else {
            System.out.println("[Redisson] 当前线程未持有锁，无法释放: " + lockKey);
        }
    }

    /**
     * 判断当前线程是否持有锁
     *
     * @return true 表示当前线程持有锁，否则返回 false
     */
    @Override
    public boolean isLockHeldByCurrentThread() {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }
}