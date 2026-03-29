package com.aegis.wms.domain.masterdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 货位实体
 * 库存管理的最小粒度单元，所有实际库存绑定至货位级别
 */
@Data
@TableName("position")
public class Position {

    /**
     * 货位ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 归属仓房ID
     */
    private Long binId;

    /**
     * 归属库区ID(冗余,便于查询)
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
    private java.math.BigDecimal capacity;

    /**
     * 状态: 1-正常 0-停用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;
}