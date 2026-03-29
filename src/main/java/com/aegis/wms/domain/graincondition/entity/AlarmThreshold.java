package com.aegis.wms.domain.graincondition.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报警阈值配置实体
 */
@Data
@TableName("alarm_threshold")
public class AlarmThreshold {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String thresholdCode;

    private String thresholdName;

    /**
     * 指标类型: max_grain_temp/min_grain_temp/avg_grain_temp/co2/o2等
     */
    private String metricType;

    /**
     * 比较运算符: >/</>=/<=
     */
    private String operator;

    private BigDecimal thresholdValue;

    /**
     * 报警级别: 1-一般 2-严重 3-紧急
     */
    private Integer alarmLevel;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}