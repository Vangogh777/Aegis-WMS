package com.aegis.wms.application.inventory.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存变动流水VO
 */
@Data
public class StockMovementVO {

    private Long id;

    private String movementNo;

    private Long positionId;

    private String positionCode;

    private Long warehouseId;

    private String warehouseName;

    private Long binId;

    private String binCode;

    /**
     * 变动类型: 1-入库 2-出库
     */
    private Integer movementType;

    private String movementTypeName;

    private Long grainVarietyId;

    private String grainVarietyName;

    private Integer harvestYear;

    private String grade;

    private BigDecimal quantity;

    private BigDecimal quantityBefore;

    private BigDecimal quantityAfter;

    private String orderType;

    private String orderNo;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}