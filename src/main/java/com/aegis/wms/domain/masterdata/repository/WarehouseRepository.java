package com.aegis.wms.domain.masterdata.repository;

import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库区仓储接口
 * 继承MyBatis-Plus BaseMapper，提供基础CRUD能力
 */
@Mapper
public interface WarehouseRepository extends BaseMapper<Warehouse> {

}