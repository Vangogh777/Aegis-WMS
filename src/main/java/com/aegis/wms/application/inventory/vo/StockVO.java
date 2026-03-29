package com.aegis.wms.application.inventory.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存VO
 */
@Data
public class StockVO {

    private Long id;

    private Long positionId;

    private String positionCode;

    private String positionName;

    private Long warehouseId;

    private String warehouseName;

    private Long binId;

    private String binCode;

    private String binName;

    private Long grainVarietyId;

    private String grainVarietyName;

    private Integer harvestYear;

    private String grade;

    /**
     * 当前库存量(吨)
     */
    private BigDecimal quantity;

    /**
     * 货位容量(吨)
     */
    private BigDecimal capacity;

    /**
     * 使用率(%)
     */
    private BigDecimal usageRate;

    private LocalDateTime updateTime;
}