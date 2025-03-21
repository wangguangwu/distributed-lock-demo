package com.wangguangwu.distributedlockdb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author wangguangwu
 */
@SpringBootApplication
@EnableTransactionManagement
public class DistributedLockDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedLockDbApplication.class, args);
    }

}
