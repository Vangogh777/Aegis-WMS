package com.aegis.wms.application.operation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业明细VO
 */
@Data
@Schema(description = "作业明细VO")
public class OperationDetailVO {

    @Schema(description = "明细ID")
    private Long id;

    @Schema(description = "作业记录ID")
    private Long recordId;

    @Schema(description = "记录编号")
    private String recordNo;

    @Schema(description = "序号(第几次启停)")
    private Integer seqNo;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "本次时长(分钟)")
    private Integer durationMinutes;

    @Schema(description = "本次时长(格式化: X小时X分钟)")
    private String durationDisplay;

    @Schema(description = "起始电表读数(度)")
    private BigDecimal startMeterReading;

    @Schema(description = "结束电表读数(度)")
    private BigDecimal endMeterReading;

    @Schema(description = "本次耗电量(度)")
    private BigDecimal powerConsumption;

    // ==================== 开始状态 ====================

    @Schema(description = "开始平均粮温(℃)")
    private BigDecimal startAvgTemp;

    @Schema(description = "开始水分(%)")
    private BigDecimal startMoisture;

    @Schema(description = "开始仓内温度(℃)")
    private BigDecimal startInnerTemp;

    @Schema(description = "开始仓内湿度(%)")
    private BigDecimal startInnerHumidity;

    @Schema(description = "开始仓外温度(℃)")
    private BigDecimal startOuterTemp;

    @Schema(description = "开始仓外湿度(%)")
    private BigDecimal startOuterHumidity;

    // ==================== 结束状态 ====================

    @Schema(description = "结束平均粮温(℃)")
    private BigDecimal endAvgTemp;

    @Schema(description = "结束水分(%)")
    private BigDecimal endMoisture;

    @Schema(description = "结束仓内温度(℃)")
    private BigDecimal endInnerTemp;

    @Schema(description = "结束仓内湿度(%)")
    private BigDecimal endInnerHumidity;

    @Schema(description = "结束仓外温度(℃)")
    private BigDecimal endOuterTemp;

    @Schema(description = "结束仓外湿度(%)")
    private BigDecimal endOuterHumidity;

    // ==================== 变化量 ====================

    @Schema(description = "本次温度变化(℃)")
    private BigDecimal tempChange;

    @Schema(description = "本次水分变化(%)")
    private BigDecimal moistureChange;

    @Schema(description = "状态: 1-进行中 2-已完成 3-已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "本次作业备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}