package com.aegis.wms.application.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 出库单DTO
 */
@Data
public class OutboundOrderDTO {

    private Long id;

    /**
     * 库区ID
     */
    private Long warehouseId;

    /**
     * 仓房ID
     */
    private Long binId;

    /**
     * 货位ID
     */
    private Long positionId;

    /**
     * 粮食品种ID
     */
    private Long grainVarietyId;

    /**
     * 收获年份
     */
    private Integer harvestYear;

    /**
     * 粮食等级
     */
    private String grade;

    /**
     * 出库数量(吨)
     */
    private BigDecimal quantity;

    /**
     * 备注
     */
    private String remark;

    /**
     * 幂等键(防止重复提交)
     */
    private String idempotentKey;
}