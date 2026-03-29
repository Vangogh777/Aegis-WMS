package com.aegis.wms.domain.inventory.repository;

import com.aegis.wms.domain.inventory.entity.InboundOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单仓储接口
 */
@Mapper
public interface InboundOrderRepository extends BaseMapper<InboundOrder> {
}