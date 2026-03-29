package com.aegis.wms.application.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 报警统计VO
 */
@Data
@Schema(description = "报警统计")
public class AlarmStatisticsVO {

    @Schema(description = "总报警数")
    private Integer totalAlarmCount;

    @Schema(description = "未处理报警数")
    private Integer pendingCount;

    @Schema(description = "已确认报警数")
    private Integer confirmedCount;

    @Schema(description = "已处理报警数")
    private Integer handledCount;

    @Schema(description = "按级别统计")
    private List<LevelStatistics> byLevel;

    @Schema(description = "按类型统计")
    private List<TypeStatistics> byType;

    @Schema(description = "按仓房统计")
    private List<BinStatistics> byBin;

    @Data
    @Schema(description = "按级别统计")
    public static class LevelStatistics {
        @Schema(description = "报警级别")
        private Integer alarmLevel;

        @Schema(description = "报警级别名称")
        private String alarmLevelName;

        @Schema(description = "数量")
        private Integer count;
    }

    @Data
    @Schema(description = "按类型统计")
    public static class TypeStatistics {
        @Schema(description = "指标类型")
        private String metricType;

        @Schema(description = "指标类型名称")
        private String metricTypeName;

        @Schema(description = "数量")
        private Integer count;
    }

    @Data
    @Schema(description = "按仓房统计")
    public static class BinStatistics {
        @Schema(description = "仓房ID")
        private Long binId;

        @Schema(description = "仓房编码")
        private String binCode;

        @Schema(description = "仓房名称")
        private String binName;

        @Schema(description = "报警数量")
        private Integer count;
    }
}