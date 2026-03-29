package com.aegis.wms.application.masterdata.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 货位创建/更新DTO
 */
@Data
public class PositionDTO {

    /**
     * 货位ID(更新时必填)
     */
    private Long id;

    /**
     * 归属仓房ID
     */
    private Long binId;

    /**
     * 归属库区ID(冗余)
     */
    private Long warehouseId;

    /**
     * 货位编号(如A区/B区)
     */
    private String positionCode;

    /**
     * 货位名称
     */
    private String positionName;

    /**
     * 货位容量(吨)
     */
    private BigDecimal capacity;

    /**
     * 状态: 1-正常 0-停用
     */
    private Integer status;
}