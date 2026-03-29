package com.aegis.wms.application.operation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 作业统计VO
 */
@Data
@Schema(description = "作业统计VO")
public class OperationStatisticsVO {

    @Schema(description = "统计维度类型: warehouse-按库区/bin-按仓房/type-按作业类型")
    private String dimensionType;

    @Schema(description = "维度ID")
    private Long dimensionId;

    @Schema(description = "维度名称")
    private String dimensionName;

    @Schema(description = "作业类型")
    private String operationType;

    @Schema(description = "作业类型名称")
    private String operationTypeName;

    // ==================== 数量统计 ====================

    @Schema(description = "总作业次数")
    private Integer totalCount;

    @Schema(description = "待执行次数")
    private Integer pendingCount;

    @Schema(description = "作业中次数")
    private Integer inProgressCount;

    @Schema(description = "已完成次数")
    private Integer completedCount;

    @Schema(description = "已取消次数")
    private Integer cancelledCount;

    // ==================== 时长统计 ====================

    @Schema(description = "累计作业时长(分钟)")
    private Integer totalDurationMinutes;

    @Schema(description = "累计作业时长(格式化)")
    private String totalDurationDisplay;

    @Schema(description = "平均作业时长(分钟)")
    private BigDecimal avgDurationMinutes;

    @Schema(description = "最长作业时长(分钟)")
    private Integer maxDurationMinutes;

    @Schema(description = "最短作业时长(分钟)")
    private Integer minDurationMinutes;

    // ==================== 能耗统计 ====================

    @Schema(description = "累计耗电量(度)")
    private BigDecimal totalPowerConsumption;

    @Schema(description = "平均耗电量(度)")
    private BigDecimal avgPowerConsumption;

    // ==================== 效果统计 ====================

    @Schema(description = "平均温度变化(℃)")
    private BigDecimal avgTempDrop;

    @Schema(description = "平均水分变化(%)")
    private BigDecimal avgMoistureDrop;

    // ==================== 启停统计 ====================

    @Schema(description = "总启停次数")
    private Integer totalDetailCount;

    @Schema(description = "平均启停次数")
    private BigDecimal avgDetailCount;

    // ==================== 详细列表 ====================

    @Schema(description = "按类型统计明细")
    private List<TypeStatistics> typeStatisticsList;

    /**
     * 按类型统计
     */
    @Data
    @Schema(description = "按类型统计")
    public static class TypeStatistics {

        @Schema(description = "作业类型")
        private String operationType;

        @Schema(description = "作业类型名称")
        private String operationTypeName;

        @Schema(description = "作业次数")
        private Integer count;

        @Schema(description = "累计时长(分钟)")
        private Integer totalDurationMinutes;

        @Schema(description = "累计耗电量(度)")
        private BigDecimal totalPowerConsumption;

        @Schema(description = "平均温度变化(℃)")
        private BigDecimal avgTempDrop;
    }
}