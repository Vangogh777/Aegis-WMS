package com.aegis.wms.application.masterdata.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库区创建/更新DTO
 */
@Data
public class WarehouseDTO {

    /**
     * 库区ID(更新时必填)
     */
    private Long id;

    /**
     * 库区编码(创建时必填)
     */
    private String warehouseCode;

    /**
     * 粮库名称
     */
    private String warehouseName;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 总设计容量(吨)
     */
    private BigDecimal totalCapacity;

    /**
     * 状态: 1-正常 0-停用
     */
    private Integer status;
}