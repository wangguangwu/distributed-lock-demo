package com.wangguangwu.distributedlockdb.controller;

import com.wangguangwu.distributedlockdb.service.OptimisticLockService;
import com.wangguangwu.distributedlockdb.service.PessimisticLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author wangguangwu
 */
@RestController
@RequestMapping("/lock")
@RequiredArgsConstructor
public class LockController {

    private final PessimisticLockService pessimisticLockService;
    private final OptimisticLockService optimisticLockService;

    @PostMapping("/pessimistic")
    public ResponseEntity<String> pessimisticLock(@RequestParam("resourceId") String resourceId) {
        pessimisticLockService.lockResource(resourceId);
        return ResponseEntity.ok("悲观锁处理成功");
    }

    @PostMapping("/optimistic")
    public ResponseEntity<String> optimisticLock(@RequestParam("resourceId") String resourceId) {
        optimisticLockService.lockResource(resourceId);
        return ResponseEntity.ok("乐观锁处理成功");
    }
}
