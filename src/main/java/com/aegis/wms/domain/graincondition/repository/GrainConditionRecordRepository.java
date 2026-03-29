package com.aegis.wms.domain.graincondition.repository;

import com.aegis.wms.domain.graincondition.entity.GrainConditionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 粮情记录仓储接口
 */
@Mapper
public interface GrainConditionRecordRepository extends BaseMapper<GrainConditionRecord> {
}