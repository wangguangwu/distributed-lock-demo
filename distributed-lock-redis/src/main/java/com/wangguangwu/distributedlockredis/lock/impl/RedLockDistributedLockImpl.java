package com.wangguangwu.distributedlockredis.lock.impl;

import com.wangguangwu.distributedlockredis.lock.AbstractDistributedLock;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 基于 RedissonMultiLock 实现 RedLock 算法的分布式锁。
 * <p>
 * 此实现依赖于 RedissonRedLockConfig 中配置的 5 个 Redis 实例，
 * 组合成一个 RedissonMultiLock。当所有子锁均成功获取时，整体锁获取成功。
 * <br>
 * 自动续锁说明：调用 lock() 方法时不指定租期，Redisson 内部会启用 Watchdog 自动续锁机制，
 * 保证锁只要未被显式释放就持续有效；而 tryLock(waitTime, leaseTime, unit) 方法则指定固定租期，
 * 不会启用自动续锁。
 * </p>
 *
 * @author wangguangwu
 */
@Component
@ConditionalOnBean(name = "redissonMultiLock")
public class RedLockDistributedLockImpl extends AbstractDistributedLock {

    private final RedissonMultiLock redissonMultiLock;

    public RedLockDistributedLockImpl(RedissonMultiLock redissonMultiLock) {
        // 注意：此处锁标识需与配置保持一致
        super("RedLock");
        // redissonMultiLock 已在配置中组合了多个单节点锁，统一使用锁名 "RedLock"
        this.redissonMultiLock = redissonMultiLock;
    }

    /**
     * 阻塞式加锁，启用 Watchdog 自动续锁机制（不指定租期）。
     *
     * @return true 表示成功获取锁，否则 false
     */
    @Override
    public boolean lock() {
        try {
            // 使用 lock() 方法不传租期，Redisson 内部会默认设置初始租期（通常为30秒），并自动续期
            redissonMultiLock.lock();
            System.out.println("[RedLock] 获取锁成功: " + lockKey);
            return true;
        } catch (Exception e) {
            System.err.println("[RedLock] 获取锁异常: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 尝试在指定等待时间内获取锁，并设置固定租期（此时不启用自动续锁）。
     *
     * @param waitTime  最大等待时间
     * @param leaseTime 锁的租期
     * @param unit      时间单位
     * @return true 表示成功获取锁，否则 false
     * @throws InterruptedException 如果线程在等待期间被中断
     */
    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        boolean acquired = redissonMultiLock.tryLock(waitTime, leaseTime, unit);
        if (acquired) {
            System.out.println("[RedLock] tryLock 成功: " + lockKey);
        } else {
            System.out.println("[RedLock] tryLock 失败: " + lockKey);
        }
        return acquired;
    }

    /**
     * 释放锁。只有当前线程持有锁时才能释放。
     */
    @Override
    public void unlock() {
        if (isLockHeldByCurrentThread()) {
            redissonMultiLock.unlock();
            System.out.println("[RedLock] 释放锁成功: " + lockKey);
        } else {
            System.out.println("[RedLock] 当前线程未持有锁，无法释放: " + lockKey);
        }
    }

    /**
     * 判断当前线程是否持有锁。
     *
     * @return true 表示当前线程持有锁，否则 false
     */
    @Override
    public boolean isLockHeldByCurrentThread() {
        // 获取内部所有的锁，RedissonMultiLock 提供 getLocks() 方法
        for (RLock lock : Objects.requireNonNull(getUnderlyingLocks())) {
            if (!lock.isHeldByCurrentThread()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 使用反射获取 RedissonMultiLock 内部的基础锁数组。
     *
     * @return 基础锁数组，如果获取失败则返回 null
     */
    @SuppressWarnings("unchecked")
    private RLock[] getUnderlyingLocks() {
        try {
            Field locksField = redissonMultiLock.getClass().getDeclaredField("locks");
            locksField.setAccessible(true);
            Object locksObj = locksField.get(redissonMultiLock);
            if (locksObj instanceof RLock[]) {
                return (RLock[]) locksObj;
            } else if (locksObj instanceof java.util.List) {
                java.util.List<RLock> locksList = (java.util.List<RLock>) locksObj;
                return locksList.toArray(new RLock[0]);
            } else {
                return null;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("[RedLock] 获取内部锁失败: " + e.getMessage());
            return null;
        }
    }
}