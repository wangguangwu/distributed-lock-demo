package com.wangguangwu.distributedlockdb.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wangguangwu
 */
@SpringBootTest
class OptimisticLockServiceTest {

    @Autowired
    private OptimisticLockService optimisticLockService;

    private static final int THREAD_COUNT = 50;

    @Test
    @DisplayName("测试乐观锁获取资源")
    void testOptimisticLock() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    optimisticLockService.lockResource("optimisticLock");
                } catch (Exception e) {
                    System.err.println("乐观锁失败：" + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
    }
}