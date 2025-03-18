package com.wangguangwu.distributedlockdb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangguangwu.distributedlockdb.entity.LockRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author wangguangwu
 */
@Mapper
public interface LockRecordMapper extends BaseMapper<LockRecord> {

    /**
     * 通过资源ID获取锁记录，并使用 FOR UPDATE 进行悲观锁控制
     *
     * @param resourceId 资源ID
     * @return 锁记录
     */
    @Select("SELECT * FROM lock_record WHERE resource_id = #{resourceId} FOR UPDATE")
    LockRecord selectForUpdate(@Param("resourceId") String resourceId);

    /**
     * 通过资源ID获取锁记录
     *
     * @param resourceId 资源ID
     * @return 锁记录
     */
    @Select("SELECT * FROM lock_record WHERE resource_id = #{resourceId}")
    LockRecord selectByResourceId(@Param("resourceId") String resourceId);

}
