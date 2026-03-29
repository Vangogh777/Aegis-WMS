package com.aegis.wms.application.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 粮情汇总VO
 */
@Data
@Schema(description = "粮情汇总")
public class GrainConditionSummaryVO {

    @Schema(description = "仓房总数")
    private Integer totalBins;

    @Schema(description = "已检测仓房数")
    private Integer detectedBins;

    @Schema(description = "平均粮温(℃)")
    private BigDecimal avgGrainTemp;

    @Schema(description = "最高粮温(℃)")
    private BigDecimal maxGrainTemp;

    @Schema(description = "最低粮温(℃)")
    private BigDecimal minGrainTemp;

    @Schema(description = "平均仓内温度(℃)")
    private BigDecimal avgInnerTemp;

    @Schema(description = "平均仓内湿度(%)")
    private BigDecimal avgInnerHumidity;

    @Schema(description = "未处理报警数")
    private Integer pendingAlarmCount;

    @Schema(description = "各仓房粮情")
    private List<BinGrainCondition> binConditions;

    @Data
    @Schema(description = "仓房粮情")
    public static class BinGrainCondition {
        @Schema(description = "仓房ID")
        private Long binId;

        @Schema(description = "仓房编码")
        private String binCode;

        @Schema(description = "仓房名称")
        private String binName;

        @Schema(description = "检测日期")
        private String recordDate;

        @Schema(description = "最高粮温(℃)")
        private BigDecimal maxGrainTemp;

        @Schema(description = "最低粮温(℃)")
        private BigDecimal minGrainTemp;

        @Schema(description = "平均粮温(℃)")
        private BigDecimal avgGrainTemp;

        @Schema(description = "仓内温度(℃)")
        private BigDecimal innerTemp;

        @Schema(description = "仓内湿度(%)")
        private BigDecimal innerHumidity;

        @Schema(description = "虫害等级")
        private String insectLevel;

        @Schema(description = "虫害等级名称")
        private String insectLevelName;

        @Schema(description = "未处理报警数")
        private Integer alarmCount;

        @Schema(description = "状态: normal-正常 warning-预警 alarm-报警")
        private String status;
    }
}