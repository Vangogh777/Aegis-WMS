package com.aegis.wms.domain.inventory.repository;

import com.aegis.wms.domain.inventory.entity.GrainVariety;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 粮食品种仓储接口
 */
@Mapper
public interface GrainVarietyRepository extends BaseMapper<GrainVariety> {
}