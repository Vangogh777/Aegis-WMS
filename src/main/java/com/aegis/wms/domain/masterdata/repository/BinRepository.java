package com.aegis.wms.domain.masterdata.repository;

import com.aegis.wms.domain.masterdata.entity.Bin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓房仓储接口
 */
@Mapper
public interface BinRepository extends BaseMapper<Bin> {

}