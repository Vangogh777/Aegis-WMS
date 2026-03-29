package com.aegis.wms.domain.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业明细实体
 * 记录每次启停详情，一个作业可多次启停
 */
@Data
@TableName("warehouse_operation_detail")
public class OperationDetail {

    /**
     * 明细ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 作业记录ID
     */
    private Long recordId;

    /**
     * 序号(第几次启停)
     */
    private Integer seqNo;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 本次时长(分钟)
     */
    private Integer durationMinutes;

    /**
     * 起始电表读数(度)
     */
    private BigDecimal startMeterReading;

    /**
     * 结束电表读数(度)
     */
    private BigDecimal endMeterReading;

    /**
     * 本次耗电量(度)
     */
    private BigDecimal powerConsumption;

    /**
     * 开始平均粮温(℃)
     */
    private BigDecimal startAvgTemp;

    /**
     * 开始水分(%)
     */
    private BigDecimal startMoisture;

    /**
     * 开始仓内温度(℃)
     */
    private BigDecimal startInnerTemp;

    /**
     * 开始仓内湿度(%)
     */
    private BigDecimal startInnerHumidity;

    /**
     * 开始仓外温度(℃)
     */
    private BigDecimal startOuterTemp;

    /**
     * 开始仓外湿度(%)
     */
    private BigDecimal startOuterHumidity;

    /**
     * 结束平均粮温(℃)
     */
    private BigDecimal endAvgTemp;

    /**
     * 结束水分(%)
     */
    private BigDecimal endMoisture;

    /**
     * 结束仓内温度(℃)
     */
    private BigDecimal endInnerTemp;

    /**
     * 结束仓内湿度(%)
     */
    private BigDecimal endInnerHumidity;

    /**
     * 结束仓外温度(℃)
     */
    private BigDecimal endOuterTemp;

    /**
     * 结束仓外湿度(%)
     */
    private BigDecimal endOuterHumidity;

    /**
     * 本次温度变化(℃)
     */
    private BigDecimal tempChange;

    /**
     * 本次水分变化(%)
     */
    private BigDecimal moistureChange;

    /**
     * 状态: 1-进行中 2-已完成 3-已取消
     */
    private Integer status;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 本次作业备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ==================== 状态常量 ====================

    /**
     * 进行中
     */
    public static final int STATUS_IN_PROGRESS = 1;

    /**
     * 已完成
     */
    public static final int STATUS_COMPLETED = 2;

    /**
     * 已取消
     */
    public static final int STATUS_CANCELLED = 3;
}