package com.wangguangwu.distributedlockredis.lock.impl;

import com.wangguangwu.distributedlockredis.constants.LockConstants;
import com.wangguangwu.distributedlockredis.lock.AbstractDistributedLock;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Jedis 实现的分布式锁（使用 ConcurrentHashMap 存储每个线程的锁值）
 * <p>
 * 本实现在加锁时生成唯一的锁值，并存储到一个全局的 ConcurrentHashMap 中，key 为当前线程 ID。
 * 解锁时使用 Lua 脚本验证只有持有锁的线程才能释放锁，并在释放后移除对应的锁值。
 * <p>
 * 此实现合并了解锁和安全解锁逻辑，确保解锁操作只能由加锁的线程成功执行。
 *
 * @author wangguangwu
 */
@Component
public class JedisDistributedLockImpl extends AbstractDistributedLock {

    /**
     * 使用 ConcurrentHashMap 存储每个线程的锁值，key 为线程 ID
     */
    private static final ConcurrentHashMap<Long, String> LOCK_VALUES = new ConcurrentHashMap<>();

    private final JedisPool jedisPool;

    public JedisDistributedLockImpl(JedisPool jedisPool) {
        super("jedisLock");
        this.jedisPool = jedisPool;
    }

    @Override
    public boolean lock() {
        try (Jedis jedis = jedisPool.getResource()) {
            long threadId = Thread.currentThread().getId();
            String localLockValue = UUID.randomUUID().toString();
            // 保存锁值到 ConcurrentHashMap
            LOCK_VALUES.put(threadId, localLockValue);
            // 使用 SetParams 设置 NX 和 PX 参数，PX 单位是毫秒，锁超时时间由常量定义（例如 10秒）
            SetParams setParams = new SetParams().nx().px(LockConstants.DEFAULT_LOCK_TIME_PX);
            String result = jedis.set(lockKey, localLockValue, setParams);
            if (LockConstants.LOCK_SUCCESS.equals(result)) {
                System.out.println("[Jedis] 获取锁成功: " + lockKey + ", lockValue=" + localLockValue);
                return true;
            } else {
                System.out.println("[Jedis] 获取锁失败: " + lockKey);
                LOCK_VALUES.remove(threadId);
                return false;
            }
        }
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        long waitTimeMillis = unit.toMillis(waitTime);
        long end = System.currentTimeMillis() + waitTimeMillis;
        while (System.currentTimeMillis() < end) {
            if (lock()) {
                // 注意：本实现中锁的超时时间固定为 DEFAULT_LOCK_TIME_PX，leaseTime 可作为业务提醒
                return true;
            }
            TimeUnit.SECONDS.sleep(15);
        }
        return false;
    }

    @Override
    public void unlock() {
        try (Jedis jedis = jedisPool.getResource()) {
            long threadId = Thread.currentThread().getId();
            String localLockValue = LOCK_VALUES.get(threadId);
            if (localLockValue == null) {
                System.out.println("[Jedis] 当前线程未持有锁: " + lockKey);
                return;
            }
            // 使用 Lua 脚本安全释放锁，只有当锁的值与当前线程保存的锁值一致时才会删除
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";
            Object result = jedis.eval(luaScript, 1, lockKey, localLockValue);
            if (result != null && ((Long) result) == 1L) {
                System.out.println("[Jedis] 释放锁成功: " + lockKey + ", lockValue=" + localLockValue);
            } else {
                System.out.println("[Jedis] 释放锁失败: " + lockKey + ", lockValue=" + localLockValue);
            }
            // 移除当前线程对应的锁值
            LOCK_VALUES.remove(threadId);
        }
    }

    @Override
    public boolean isLockHeldByCurrentThread() {
        throw new UnsupportedOperationException();
    }
}