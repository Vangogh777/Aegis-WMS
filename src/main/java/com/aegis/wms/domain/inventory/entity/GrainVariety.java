package com.aegis.wms.domain.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 粮食品种实体
 */
@Data
@TableName("grain_variety")
public class GrainVariety {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String varietyCode;

    private String varietyName;

    private Integer sortOrder;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}