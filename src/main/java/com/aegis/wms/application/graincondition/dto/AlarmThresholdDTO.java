package com.aegis.wms.application.graincondition.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 报警阈值DTO
 */
@Data
public class AlarmThresholdDTO {

    private Long id;

    private String thresholdCode;

    private String thresholdName;

    /**
     * 指标类型
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
}