package com.wangguangwu.distributedlockdb.service;

import com.wangguangwu.distributedlockdb.entity.LockRecord;
import com.wangguangwu.distributedlockdb.mapper.LockRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 悲观锁实现
 *
 * @author wangguangwu
 */
@Service
@RequiredArgsConstructor
public class PessimisticLockService {

    private final LockRecordMapper lockRecordMapper;

    @Transactional(timeout = 60)
    public void lockResource(String resourceId) {
        String threadName = Thread.currentThread().getName();

        // 通过 SELECT ... FOR UPDATE 获取数据库锁，此方法会阻塞其他线程直到锁释放
        LockRecord record = lockRecordMapper.selectForUpdate(resourceId);
        if (record == null) {
            throw new RuntimeException("资源不存在");
        }

        System.out.println("[" + threadName + "] 成功获取悲观锁，资源 " + record.getResourceId() + " 已被锁定。");

        // 模拟业务逻辑处理，控制事务时长
        try {
            System.out.println("[" + threadName + "] 正在处理业务...");
            // 模拟耗时 1 秒的业务处理
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("业务处理被中断", e);
        }

        // 更新记录，释放锁后其他线程可继续获得锁
        record.setDescription("业务处理完成");
        lockRecordMapper.updateById(record);

        System.out.println("[" + threadName + "] 业务处理完成，资源 " + record.getResourceId() +
                " 的描述已更新为：" + record.getDescription());
    }
}
