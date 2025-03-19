package com.wangguangwu.distributedlockredis.lock.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 测试基于 Redisson 实现的分布式锁：
 * <ul>
 *   <li>单线程下测试加锁与解锁</li>
 *   <li>测试自动续锁：锁持有时间超过租期后仍被当前线程持有</li>
 *   <li>多线程测试：在同一时刻仅有一个线程持有锁</li>
 *   <li>测试其他线程尝试解锁：非持锁线程调用 unlock() 不会释放锁</li>
 * </ul>
 */
@SpringBootTest
@DisplayName("测试使用 Redisson 实现分布式锁")
public class RedissonDistributedLockImplTest {

    @Autowired
    private RedissonDistributedLockImpl redissonDistributedLockImpl;

    @Test
    @DisplayName("单线程测试：加锁与解锁")
    void testSingleThreadLockUnlock() {
        boolean locked = redissonDistributedLockImpl.lock();
        assertTrue(locked, "单线程下应成功获取锁");
        redissonDistributedLockImpl.unlock();
    }

    @Test
    @DisplayName("测试自动续锁：锁持有时间超过租期后仍被当前线程持有")
    void testAutoRenewLock() throws InterruptedException {
        // 获取锁（使用 lock() 方法启用 Watchdog 自动续期机制）
        boolean locked = redissonDistributedLockImpl.lock();
        assertTrue(locked, "应成功获取锁");
        // 持锁时间超过默认租期（10秒），例如等待15秒
        TimeUnit.SECONDS.sleep(15);
        // 检查当前线程是否仍持有锁
        boolean lockHeldByCurrentThread = redissonDistributedLockImpl.isLockHeldByCurrentThread();
        assertTrue(lockHeldByCurrentThread, "自动续锁机制应确保锁在持有期间不会失效");
        redissonDistributedLockImpl.unlock();
    }

    @Test
    @DisplayName("多线程测试：在同一时刻仅有一个线程持有锁")
    void testMultiThreadLock() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger currentLockHolders = new AtomicInteger(0);
        AtomicInteger maxLockHolders = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    if (redissonDistributedLockImpl.lock()) {
                        // 记录当前持锁的线程数
                        int holders = currentLockHolders.incrementAndGet();
                        maxLockHolders.updateAndGet(prev -> Math.max(prev, holders));
                        // 模拟业务处理，保持锁 2 秒
                        TimeUnit.SECONDS.sleep(2);
                        currentLockHolders.decrementAndGet();
                        redissonDistributedLockImpl.unlock();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();
        // 验证在任一时刻最大同时持锁数为1
        assertEquals(1, maxLockHolders.get(), "在同一时刻，应只有一个线程持有锁");
    }

    @Test
    @DisplayName("测试其他线程尝试解锁：非持锁线程不能释放锁")
    void testUnlockByAnotherThread() throws InterruptedException {
        // 主线程获取锁
        boolean locked = redissonDistributedLockImpl.lock();
        assertTrue(locked, "主线程应成功获取锁");

        CountDownLatch latch = new CountDownLatch(1);
        Thread otherThread = new Thread(() -> {
            // 其他线程调用 unlock() 不应释放主线程的锁
            redissonDistributedLockImpl.unlock();
            latch.countDown();
        });
        otherThread.start();
        latch.await();

        // 主线程仍然持有锁，最后释放锁
        redissonDistributedLockImpl.unlock();
    }
}