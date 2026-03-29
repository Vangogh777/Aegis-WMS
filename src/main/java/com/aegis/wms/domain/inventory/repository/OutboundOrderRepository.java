package com.aegis.wms.domain.inventory.repository;

import com.aegis.wms.domain.inventory.entity.OutboundOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单仓储接口
 */
@Mapper
public interface OutboundOrderRepository extends BaseMapper<OutboundOrder> {
}