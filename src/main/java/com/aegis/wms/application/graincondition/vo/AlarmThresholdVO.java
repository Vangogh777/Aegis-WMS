package com.aegis.wms.application.graincondition.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报警阈值VO
 */
@Data
public class AlarmThresholdVO {

    private Long id;

    private String thresholdCode;

    private String thresholdName;

    private String metricType;

    private String metricTypeName;

    private String operator;

    private BigDecimal thresholdValue;

    private Integer alarmLevel;

    private String alarmLevelName;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}