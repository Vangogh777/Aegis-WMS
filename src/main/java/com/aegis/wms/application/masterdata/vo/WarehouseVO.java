package com.aegis.wms.application.masterdata.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库区视图对象
 */
@Data
public class WarehouseVO {

    /**
     * 库区ID
     */
    private Long id;

    /**
     * 库区编码
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}