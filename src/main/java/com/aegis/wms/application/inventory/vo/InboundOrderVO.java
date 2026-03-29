package com.aegis.wms.application.inventory.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单VO
 */
@Data
public class InboundOrderVO {

    private Long id;

    private String orderNo;

    private Long warehouseId;

    private String warehouseName;

    private Long binId;

    private String binCode;

    private String binName;

    private Long positionId;

    private String positionCode;

    private String positionName;

    private Long grainVarietyId;

    private String grainVarietyName;

    private Integer harvestYear;

    private String grade;

    private BigDecimal quantity;

    /**
     * 状态: 0-已取消 1-待审核 2-已完成
     */
    private Integer status;

    private String statusName;

    private String remark;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}