package com.aegis.wms.application.operation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业记录DTO
 */
@Data
@Schema(description = "作业记录DTO")
public class OperationRecordDTO {

    @Schema(description = "记录ID(更新时必填)")
    private Long id;

    @Schema(description = "关联方案ID(可为空,表示临时作业)")
    private Long schemeId;

    @Schema(description = "库区ID")
    private Long warehouseId;

    @Schema(description = "仓房ID")
    private Long binId;

    @Schema(description = "作业类型: ventilation-通风/aeration-气调/temperature-控温/fumigation-熏蒸")
    private String operationType;

    @Schema(description = "状态: 0-待执行 1-作业中 2-已完成 3-已取消")
    private Integer status;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "备注")
    private String remark;

    // ==================== 作业汇总字段(用于更新) ====================

    @Schema(description = "首次开始时间")
    private LocalDateTime startTime;

    @Schema(description = "最后结束时间")
    private LocalDateTime endTime;

    @Schema(description = "累计作业时长(分钟)")
    private Integer totalDurationMinutes;

    @Schema(description = "累计耗电量(度)")
    private BigDecimal totalPower;

    @Schema(description = "启停次数")
    private Integer detailCount;

    @Schema(description = "起始平均粮温(℃)")
    private BigDecimal startAvgTemp;

    @Schema(description = "起始水分(%)")
    private BigDecimal startMoisture;

    @Schema(description = "结束平均粮温(℃)")
    private BigDecimal endAvgTemp;

    @Schema(description = "结束水分(%)")
    private BigDecimal endMoisture;

    @Schema(description = "温度变化(℃)")
    private BigDecimal tempDrop;

    @Schema(description = "水分变化(%)")
    private BigDecimal moistureDrop;
}