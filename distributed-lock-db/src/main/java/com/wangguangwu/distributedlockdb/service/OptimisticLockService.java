package com.wangguangwu.distributedlockdb.service;

import com.wangguangwu.distributedlockdb.entity.LockRecord;
import com.wangguangwu.distributedlockdb.mapper.LockRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 乐观锁实现
 *
 * @author wangguangwu
 */
@Service
@RequiredArgsConstructor
public class OptimisticLockService {

    private final LockRecordMapper lockRecordMapper;

    @Transactional
    public void lockResource(String resourceId) {
        final int maxRetries = 3;
        int attempt = 0;
        boolean success = false;
        while (attempt < maxRetries && !success) {
            attempt++;
            String threadName = Thread.currentThread().getName();
            // 查询记录
            LockRecord record = lockRecordMapper.selectByResourceId(resourceId);
            if (record == null) {
                throw new RuntimeException("资源不存在");
            }
            if (record.getVersion() == null) {
                throw new RuntimeException("数据版本号丢失，无法使用乐观锁");
            }
            System.out.println("[" + threadName + "] 尝试更新，第 " + attempt + " 次，更新前版本号：" + record.getVersion());
            // 执行更新操作（MyBatis-Plus 会自动处理版本号加1）
            int updatedRows = lockRecordMapper.updateById(record);
            if (updatedRows > 0) {
                LockRecord updatedRecord = lockRecordMapper.selectById(record.getId());
                System.out.println("[" + threadName + "] 更新成功，第 " + attempt + " 次，更新后版本号：" + updatedRecord.getVersion());
                System.out.println("[" + threadName + "] 成功获取乐观锁，处理业务...");
                success = true;
            } else {
                System.out.println("[" + threadName + "] 更新失败，第 " + attempt + " 次，版本冲突。");
            }
        }
        if (!success) {
            System.out.println("乐观锁连续 " + maxRetries + " 次更新失败，请重试");
        }
    }
}
