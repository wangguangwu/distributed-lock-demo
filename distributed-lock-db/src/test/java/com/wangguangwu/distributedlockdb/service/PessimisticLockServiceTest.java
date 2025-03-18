package com.wangguangwu.distributedlockdb.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wangguangwu
 */
@SpringBootTest
class PessimisticLockServiceTest {

    @Autowired
    private PessimisticLockService pessimisticLockService;

    private static final int THREAD_COUNT = 30;

    @Test
    void testPessimisticLock() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    pessimisticLockService.lockResource("pessimisticLock");
                } catch (Exception e) {
                    System.err.println("锁获取失败：" + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
    }
}