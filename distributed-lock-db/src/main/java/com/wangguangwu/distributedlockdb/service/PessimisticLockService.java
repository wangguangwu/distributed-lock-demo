package com.wangguangwu.distributedlockdb.service;

import com.wangguangwu.distributedlockdb.entity.LockRecord;
import com.wangguangwu.distributedlockdb.mapper.LockRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 悲观锁实现
 *
 * @author wangguangwu
 */
@Service
@RequiredArgsConstructor
public class PessimisticLockService {

    private final LockRecordMapper lockRecordMapper;

    @Transactional
    public void lockResource(String resourceId) {
        // 通过 `SELECT FOR UPDATE` 获取数据库锁
        LockRecord record = lockRecordMapper.selectForUpdate(resourceId);
        if (record == null) {
            throw new RuntimeException("资源不存在");
        }
        // 执行业务逻辑
        System.out.println("成功获取悲观锁，处理业务...");
    }
}
