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
 * 测试基于 Jedis 实现的分布式锁：
 * - 单线程下测试加锁与解锁功能
 * - 单线程下测试锁的超时自动释放（锁设置 10 秒超时，等待 12 秒后应自动释放）
 * - 多线程下测试，在同一时刻仅有一个线程能成功获取锁
 */
@SpringBootTest
@DisplayName("测试使用 Jedis 实现分布式锁")
public class JedisDistributedLockImplTest {

    @Autowired
    private JedisDistributedLockImpl jedisDistributedLockImpl;

    @Test
    @DisplayName("单线程测试：加锁与解锁")
    void testSingleThreadLockUnlock() {
        boolean locked = jedisDistributedLockImpl.lock();
        assertTrue(locked, "单线程下应成功获取锁");
        jedisDistributedLockImpl.unlock();
    }

    @Test
    @DisplayName("单线程测试：超时自动释放锁")
    void testLockTimeout() throws InterruptedException {
        // 获取锁（锁超时设置为10秒，见实现）
        boolean locked = jedisDistributedLockImpl.lock();
        assertTrue(locked, "应成功获取锁");
        // 等待 12 秒，让锁自动过期释放
        TimeUnit.SECONDS.sleep(12);
        // 此时锁应已过期，可以再次获取
        boolean lockedAgain = jedisDistributedLockImpl.lock();
        assertTrue(lockedAgain, "锁超时后应自动释放，能够再次获取锁");
        jedisDistributedLockImpl.unlock();
    }

    @Test
    @DisplayName("多线程测试：同一时刻仅有一个线程获取锁")
    void testMultiThreadLock() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    if (jedisDistributedLockImpl.lock()) {
                        successCount.incrementAndGet();
                        // 模拟业务处理，保持锁200毫秒
                        TimeUnit.SECONDS.sleep(2);
                        jedisDistributedLockImpl.unlock();
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
        // 在同一时刻，由于 Redis 的 SET NX 原子操作，理论上只有一个线程能成功获取锁
        assertEquals(1, successCount.get(), "在同一时刻，应只有一个线程获取到锁");
    }

    @Test
    @DisplayName("测试其他线程尝试解锁：不允许其他线程释放锁")
    void testUnlockByAnotherThread() throws InterruptedException {
        // 主线程获取锁
        boolean locked = jedisDistributedLockImpl.lock();
        assertTrue(locked, "主线程应成功获取锁");
        // 获取主线程的锁值

        // 使用另一个线程尝试释放锁
        CountDownLatch latch = new CountDownLatch(1);
        Thread otherThread = new Thread(() -> {
            // 该线程调用 unlock，由于未获取锁，其线程 ID 不存在于锁值映射中，因此不会释放锁
            jedisDistributedLockImpl.unlock();
            latch.countDown();
        });
        otherThread.start();
        latch.await();

        // 最后，主线程释放锁
        jedisDistributedLockImpl.unlock();
    }
}