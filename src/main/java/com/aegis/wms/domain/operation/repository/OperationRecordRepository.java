package com.aegis.wms.domain.operation.repository;

import com.aegis.wms.domain.operation.entity.OperationRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作业记录仓储接口
 * 继承MyBatis-Plus BaseMapper，提供基础CRUD能力
 */
@Mapper
public interface OperationRecordRepository extends BaseMapper<OperationRecord> {

}