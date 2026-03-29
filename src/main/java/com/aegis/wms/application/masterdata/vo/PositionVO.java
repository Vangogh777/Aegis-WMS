package com.aegis.wms.application.masterdata.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货位视图对象
 */
@Data
public class PositionVO {

    /**
     * 货位ID
     */
    private Long id;

    /**
     * 归属仓房ID
     */
    private Long binId;

    /**
     * 仓房编号(关联查询)
     */
    private String binCode;

    /**
     * 仓房名称(关联查询)
     */
    private String binName;

    /**
     * 归属库区ID
     */
    private Long warehouseId;

    /**
     * 库区名称(关联查询)
     */
    private String warehouseName;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}