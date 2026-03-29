package com.aegis.wms.application.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 作业汇总VO
 */
@Data
@Schema(description = "作业汇总")
public class OperationSummaryVO {

    @Schema(description = "本月入库次数")
    private Integer inboundCount;

    @Schema(description = "本月入库数量(吨)")
    private BigDecimal inboundQuantity;

    @Schema(description = "本月出库次数")
    private Integer outboundCount;

    @Schema(description = "本月出库数量(吨)")
    private BigDecimal outboundQuantity;

    @Schema(description = "本月粮情检测次数")
    private Integer grainConditionCount;

    @Schema(description = "本月报警次数")
    private Integer alarmCount;

    @Schema(description = "本月已处理报警数")
    private Integer handledAlarmCount;

    @Schema(description = "本月作业总时长(小时)")
    private BigDecimal totalDuration;

    @Schema(description = "平均作业时长(小时)")
    private BigDecimal avgDuration;
}