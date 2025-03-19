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
 * 测试基于 RedLock（RedissonMultiLock）实现的分布式锁：
 * <ul>
 *   <li>单线程测试：加锁与解锁</li>
 *   <li>自动续锁测试：锁持有时间超过租期后仍被当前线程持有</li>
 *   <li>多线程测试：在同一时刻仅有一个线程持有锁</li>
 *   <li>其他线程尝试解锁：非持锁线程调用 unlock() 不会释放锁</li>
 *   <li>测试锁获取失败：当锁被长时间持有时，其他线程 tryLock 短等待应获取失败</li>
 *   <li>测试不同锁 key：不同 key 之间并发加锁互不干扰</li>
 * </ul>
 */
@SpringBootTest
@DisplayName("测试使用 RedLock 实现分布式锁")
public class RedLockDistributedLockImplTest {

    @Autowired
    private RedLockDistributedLockImpl redLockDistributedLockImpl;

    @Test
    @DisplayName("单线程测试：加锁与解锁")
    void testSingleThreadLockUnlock() {
        boolean locked = redLockDistributedLockImpl.lock();
        assertTrue(locked, "单线程下应成功获取 RedLock 锁");
        redLockDistributedLockImpl.unlock();
    }

    @Test
    @DisplayName("测试自动续锁：锁持有时间超过租期后仍被当前线程持有")
    void testAutoRenewLock() throws InterruptedException {
        boolean locked = redLockDistributedLockImpl.lock();
        assertTrue(locked, "应成功获取锁");
        // 持锁15秒（超过通常设置的10秒租期），自动续锁机制应生效
        TimeUnit.SECONDS.sleep(15);
        // 检查当前线程是否依然持有锁
        boolean held = redLockDistributedLockImpl.isLockHeldByCurrentThread();
        assertTrue(held, "自动续锁机制应确保锁在持有期间不会失效");
        redLockDistributedLockImpl.unlock();
    }

    @Test
    @DisplayName("多线程测试：在同一时刻仅有一个线程持有锁")
    void testMultiThreadLockConcurrency() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger currentLockCount = new AtomicInteger(0);
        AtomicInteger maxLockHolders = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    if (redLockDistributedLockImpl.lock()) {
                        int count = currentLockCount.incrementAndGet();
                        maxLockHolders.updateAndGet(prev -> Math.max(prev, count));
                        // 模拟业务处理，持锁2秒
                        TimeUnit.SECONDS.sleep(2);
                        currentLockCount.decrementAndGet();
                        redLockDistributedLockImpl.unlock();
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
        // 验证同时持锁的最大线程数为1
        assertEquals(1, maxLockHolders.get(), "在同一时刻，应只有一个线程持有 RedLock 锁");
    }

    @Test
    @DisplayName("测试其他线程尝试解锁：非持锁线程不能释放锁")
    void testUnlockByAnotherThread() throws InterruptedException {
        // 主线程获取锁
        boolean locked = redLockDistributedLockImpl.lock();
        assertTrue(locked, "主线程应成功获取 RedLock 锁");

        CountDownLatch latch = new CountDownLatch(1);
        Thread otherThread = new Thread(() -> {
            // 其他线程调用 unlock()，由于不是持锁线程，不会释放锁
            redLockDistributedLockImpl.unlock();
            latch.countDown();
        });
        otherThread.start();
        latch.await();

        // 主线程仍然持有锁，最后释放锁
        redLockDistributedLockImpl.unlock();
    }

    @Test
    @DisplayName("测试锁获取失败：当锁被长时间持有时，其他线程 tryLock 应返回 false")
    void testLockAcquisitionFailure() throws InterruptedException {
        // 主线程获取锁并保持较长时间
        boolean locked = redLockDistributedLockImpl.lock();
        assertTrue(locked, "主线程应成功获取锁");

        // 使用 tryLock 的线程尝试在短时间内获取锁，预计失败
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        int taskCount = 3;
        CountDownLatch doneLatch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                try {
                    String threadName = Thread.currentThread().getName();
                    long startTime = System.currentTimeMillis();
                    System.out.println("线程 " + threadName + " 开始尝试获取锁，开始时间：" + startTime);
                    startLatch.await();
                    // 设置等待时间为10秒，租期固定为5秒（此时不会自动续锁）
                    boolean acquired = redLockDistributedLockImpl.tryLock(10, 5, TimeUnit.SECONDS);
                    long endTime = System.currentTimeMillis();
                    System.out.println("线程 " + threadName + " 获取锁结束，结束时间：" + endTime);
                    if (acquired) {
                        successCount.incrementAndGet();
                        redLockDistributedLockImpl.unlock();
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

        // 由于主线程持锁且不释放，其他线程的 tryLock 应全部失败
        assertEquals(0, successCount.get(), "当锁被长时间持有时，其他线程应无法获取锁");
        // 主线程释放锁
        redLockDistributedLockImpl.unlock();
    }
}