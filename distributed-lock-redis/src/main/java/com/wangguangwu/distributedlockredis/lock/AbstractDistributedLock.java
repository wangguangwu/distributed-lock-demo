package com.wangguangwu.distributedlockredis.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁的抽象类，定义了 lock 与 unlock 方法。
 *
 * @author wangguangwu
 */
public abstract class AbstractDistributedLock {

    // 锁的标识，由子类构造时设置
    protected final String lockKey;

    public AbstractDistributedLock(String lockKey) {
        this.lockKey = lockKey;
    }

    /**
     * 阻塞式加锁，直到获取成功
     *
     * @return true 表示成功获取锁，否则 false
     */
    public abstract boolean lock();

    /**
     * 尝试在指定等待时间内获取锁，超时返回 false
     *
     * @param waitTime  最大等待时间
     * @param leaseTime 锁的持有时间
     * @param unit      时间单位
     * @return true 表示成功获取锁，否则 false
     * @throws InterruptedException 如果线程在等待期间被中断
     */
    public abstract boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

    /**
     * 释放锁，使用 Lua 脚本安全解锁，确保只有锁持有者能释放锁
     */
    public abstract void unlock();

}
