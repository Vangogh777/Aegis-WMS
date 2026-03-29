package com.aegis.wms.application.masterdata.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 仓房创建/更新DTO
 */
@Data
public class BinDTO {

    /**
     * 仓房ID(更新时必填)
     */
    private Long id;

    /**
     * 归属库区ID
     */
    private Long warehouseId;

    /**
     * 仓房编号(如01仓)
     */
    private String binCode;

    /**
     * 仓房名称
     */
    private String binName;

    /**
     * 仓型: 高大平房仓/浅圆仓/立筒仓等
     */
    private String binType;

    /**
     * 仓容(吨)
     */
    private BigDecimal capacity;

    /**
     * 状态: 1-正常 0-停用
     */
    private Integer status;
}