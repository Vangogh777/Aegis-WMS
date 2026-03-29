package com.aegis.wms.domain.operation.repository;

import com.aegis.wms.domain.operation.entity.OperationDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作业明细仓储接口
 * 继承MyBatis-Plus BaseMapper，提供基础CRUD能力
 */
@Mapper
public interface OperationDetailRepository extends BaseMapper<OperationDetail> {

}