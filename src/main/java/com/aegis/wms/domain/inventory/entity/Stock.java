package com.aegis.wms.domain.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存实体(货位级)
 * 支持高并发分段锁，每个货位一条记录
 */
@Data
@TableName("stock")
public class Stock {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 货位ID - 锁的粒度
     */
    private Long positionId;

    /**
     * 库区ID(冗余)
     */
    private Long warehouseId;

    /**
     * 仓房ID(冗余)
     */
    private Long binId;

    /**
     * 粮食品种ID
     */
    private Long grainVarietyId;

    /**
     * 收获年份
     */
    private Integer harvestYear;

    /**
     * 粮食等级
     */
    private String grade;

    /**
     * 当前库存量(吨)
     */
    private BigDecimal quantity;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}