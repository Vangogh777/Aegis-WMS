package com.aegis.wms.domain.masterdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库区实体
 * 粮库的三级空间架构顶层：库区 -> 仓房 -> 货位
 */
@Data
@TableName("warehouse")
public class Warehouse {

    /**
     * 库区ID
     */
    @TableId(type = IdType.AUTO)
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
    private java.math.BigDecimal totalCapacity;

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
     * 删除标记: 0-正常 1-已删除
     */
    @TableLogic
    private Integer deleted;
}