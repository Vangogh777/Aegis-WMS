package com.aegis.wms.domain.masterdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓房实体
 * 归属库区，是粮食存储的中间层级
 */
@Data
@TableName("bin")
public class Bin {

    /**
     * 仓房ID
     */
    @TableId(type = IdType.AUTO)
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