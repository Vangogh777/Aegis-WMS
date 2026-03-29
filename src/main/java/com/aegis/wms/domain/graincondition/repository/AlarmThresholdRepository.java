package com.aegis.wms.domain.graincondition.repository;

import com.aegis.wms.domain.graincondition.entity.AlarmThreshold;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报警阈值仓储接口
 */
@Mapper
public interface AlarmThresholdRepository extends BaseMapper<AlarmThreshold> {
}