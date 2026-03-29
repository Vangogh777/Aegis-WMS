package com.aegis.wms.domain.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库单实体
 */
@Data
@TableName("outbound_order")
public class OutboundOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long warehouseId;

    private Long binId;

    private Long positionId;

    private Long grainVarietyId;

    private Integer harvestYear;

    private String grade;

    /**
     * 出库数量(吨)
     */
    private BigDecimal quantity;

    /**
     * 状态: 0-已取消 1-待审核 2-已完成
     */
    private Integer status;

    private String remark;

    private Long operatorId;

    private String operatorName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}