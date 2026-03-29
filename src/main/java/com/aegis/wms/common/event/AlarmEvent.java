package com.aegis.wms.common.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报警事件
 * 用于WebSocket推送
 */
@Data
public class AlarmEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long alarmId;

    private String alarmNo;

    private Long warehouseId;

    private String warehouseName;

    private Long binId;

    private String binCode;

    private String binName;

    private String metricType;

    private String metricTypeName;

    private String alarmContent;

    private Integer alarmLevel;

    private String alarmLevelName;

    private LocalDateTime createTime;
}