package com.aegis.wms.domain.operation.repository;

import com.aegis.wms.domain.operation.entity.OperationScheme;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作业方案仓储接口
 * 继承MyBatis-Plus BaseMapper，提供基础CRUD能力
 */
@Mapper
public interface OperationSchemeRepository extends BaseMapper<OperationScheme> {

}