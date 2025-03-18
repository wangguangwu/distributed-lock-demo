package com.wangguangwu.distributedlockdb.controller;

import com.wangguangwu.distributedlockdb.service.OptimisticLockService;
import com.wangguangwu.distributedlockdb.service.PessimisticLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangguangwu
 */
@RestController
@RequestMapping("/lock")
@RequiredArgsConstructor
public class LockController {

    private final PessimisticLockService pessimisticLockService;
    private final OptimisticLockService optimisticLockService;

    @PostMapping("/pessimistic/{resourceId}")
    public ResponseEntity<String> pessimisticLock(@PathVariable String resourceId) {
        pessimisticLockService.lockResource(resourceId);
        return ResponseEntity.ok("悲观锁处理成功");
    }

    @PostMapping("/optimistic/{resourceId}")
    public ResponseEntity<String> optimisticLock(@PathVariable String resourceId) {
        optimisticLockService.lockResource(resourceId);
        return ResponseEntity.ok("乐观锁处理成功");
    }
}
