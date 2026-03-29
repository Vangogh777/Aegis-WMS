package com.aegis.wms.domain.inventory.repository;

import com.aegis.wms.domain.inventory.entity.Stock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存仓储接口
 */
@Mapper
public interface StockRepository extends BaseMapper<Stock> {
}