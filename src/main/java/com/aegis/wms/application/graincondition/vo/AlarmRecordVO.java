package com.aegis.wms.application.graincondition.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报警记录VO
 */
@Data
public class AlarmRecordVO {

    private Long id;

    private String alarmNo;

    private Long warehouseId;

    private String warehouseName;

    private Long binId;

    private String binCode;

    private String binName;

    private Long recordId;

    private String recordNo;

    private Long thresholdId;

    private String thresholdName;

    private String metricType;

    private String metricTypeName;

    private BigDecimal metricValue;

    private BigDecimal thresholdValue;

    private Integer alarmLevel;

    private String alarmLevelName;

    private String alarmContent;

    private Integer status;

    private String statusName;

    private Long handlerId;

    private String handlerName;

    private LocalDateTime handleTime;

    private String handleRemark;

    private LocalDateTime createTime;
}