package com.aegis.wms.domain.graincondition.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 粮情记录实体
 */
@Data
@TableName("grain_condition_record")
public class GrainConditionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String recordNo;

    private Long warehouseId;

    private Long binId;

    private LocalDate recordDate;

    // 环境温湿度
    private BigDecimal outerTemp;

    private BigDecimal outerHumidity;

    private BigDecimal innerTemp;

    private BigDecimal innerHumidity;

    // 粮温信息
    private BigDecimal maxGrainTemp;

    private BigDecimal minGrainTemp;

    private BigDecimal avgGrainTemp;

    // 虫情气体
    private String insectLevel;

    private String insectDesc;

    private BigDecimal o2Concentration;

    private BigDecimal co2Concentration;

    // 其他信息
    private BigDecimal moistureContent;

    private String remark;

    /**
     * 数据来源: manual-手工录入 iot-传感器采集
     */
    private String dataSource;

    private Long operatorId;

    private String operatorName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}