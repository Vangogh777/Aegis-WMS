package com.aegis.wms.application.masterdata.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓房视图对象
 */
@Data
public class BinVO {

    /**
     * 仓房ID
     */
    private Long id;

    /**
     * 归属库区ID
     */
    private Long warehouseId;

    /**
     * 库区名称(关联查询)
     */
    private String warehouseName;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}