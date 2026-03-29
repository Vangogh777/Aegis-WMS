package com.aegis.wms.domain.inventory.repository;

import com.aegis.wms.domain.inventory.entity.StockMovement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存变动流水仓储接口
 */
@Mapper
public interface StockMovementRepository extends BaseMapper<StockMovement> {
}