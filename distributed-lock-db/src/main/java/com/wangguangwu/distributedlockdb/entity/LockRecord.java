package com.wangguangwu.distributedlockdb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * @author wangguangwu
 */
@Data
@TableName("lock_record")
public class LockRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String resourceId;

    @Version
    private Integer version;

    private String description;

}