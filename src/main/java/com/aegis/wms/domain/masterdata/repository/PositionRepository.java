package com.aegis.wms.domain.masterdata.repository;

import com.aegis.wms.domain.masterdata.entity.Position;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 货位仓储接口
 */
@Mapper
public interface PositionRepository extends BaseMapper<Position> {

}