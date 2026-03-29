package com.aegis.wms.common.event;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 粮情已录入事件
 * Kafka Topic: topic_grain_condition_recorded
 */
@Data
public class GrainConditionRecordedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 粮情记录ID
     */
    private Long recordId;

    /**
     * 记录编号
     */
    private String recordNo;

    /**
     * 库区ID
     */
    private Long warehouseId;

    /**
     * 仓房ID
     */
    private Long binId;

    /**
     * 查仓日期
     */
    private LocalDate recordDate;

    // 粮温信息
    private BigDecimal maxGrainTemp;

    private BigDecimal minGrainTemp;

    private BigDecimal avgGrainTemp;

    // 气体信息
    private BigDecimal o2Concentration;

    private BigDecimal co2Concentration;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 事件时间
     */
    private Long eventTime;
}