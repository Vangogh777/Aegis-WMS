package com.aegis.wms.domain.graincondition.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报警记录实体
 */
@Data
@TableName("alarm_record")
public class AlarmRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String alarmNo;

    private Long warehouseId;

    private Long binId;

    private Long recordId;

    private Long thresholdId;

    private String metricType;

    private BigDecimal metricValue;

    private BigDecimal thresholdValue;

    private Integer alarmLevel;

    private String alarmContent;

    /**
     * 状态: 0-未处理 1-已确认 2-已处理
     */
    private Integer status;

    private Long handlerId;

    private String handlerName;

    private LocalDateTime handleTime;

    private String handleRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}