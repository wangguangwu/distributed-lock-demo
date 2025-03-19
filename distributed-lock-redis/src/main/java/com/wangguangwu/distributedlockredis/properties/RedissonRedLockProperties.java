package com.wangguangwu.distributedlockredis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wangguangwu
 */

@Data
@Component
@ConfigurationProperties(prefix = "redisson-red-lock")
public class RedissonRedLockProperties {

    private String mode;
    private List<String> addresses;

}
