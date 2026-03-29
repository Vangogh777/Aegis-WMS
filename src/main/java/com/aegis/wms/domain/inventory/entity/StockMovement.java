package com.aegis.wms.domain.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存变动流水实体
 * 强一致性审计日志
 */
@Data
@TableName("stock_movement")
public class StockMovement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String movementNo;

    private Long positionId;

    private Long warehouseId;

    private Long binId;

    /**
     * 变动类型: 1-入库 2-出库
     */
    private Integer movementType;

    private Long grainVarietyId;

    private Integer harvestYear;

    private String grade;

    /**
     * 变动数量(吨)
     */
    private BigDecimal quantity;

    /**
     * 变动前库存
     */
    private BigDecimal quantityBefore;

    /**
     * 变动后库存
     */
    private BigDecimal quantityAfter;

    /**
     * 关联单据类型: INBOUND/OUTBOUND
     */
    private String orderType;

    /**
     * 关联单据ID
     */
    private Long orderId;

    private Long operatorId;

    private String operatorName;

    /**
     * 幂等键
     */
    private String idempotentKey;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}