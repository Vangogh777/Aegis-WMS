package com.aegis.wms.application.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 月报表VO
 */
@Data
@Schema(description = "月报表")
public class MonthlyReportVO {

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "库区ID")
    private Long warehouseId;

    @Schema(description = "库区名称")
    private String warehouseName;

    // 库存信息
    @Schema(description = "月初库存(吨)")
    private BigDecimal beginningStock;

    @Schema(description = "月末库存(吨)")
    private BigDecimal endingStock;

    @Schema(description = "库存变动(吨)")
    private BigDecimal stockChange;

    // 入库信息
    @Schema(description = "本月入库次数")
    private Integer inboundCount;

    @Schema(description = "本月入库数量(吨)")
    private BigDecimal inboundQuantity;

    // 出库信息
    @Schema(description = "本月出库次数")
    private Integer outboundCount;

    @Schema(description = "本月出库数量(吨)")
    private BigDecimal outboundQuantity;

    // 粮情信息
    @Schema(description = "检测次数")
    private Integer detectionCount;

    @Schema(description = "平均粮温(℃)")
    private BigDecimal avgGrainTemp;

    @Schema(description = "最高粮温(℃)")
    private BigDecimal maxGrainTemp;

    @Schema(description = "最低粮温(℃)")
    private BigDecimal minGrainTemp;

    // 报警信息
    @Schema(description = "本月报警次数")
    private Integer alarmCount;

    @Schema(description = "本月处理报警次数")
    private Integer handledAlarmCount;

    @Schema(description = "报警处理率(%)")
    private BigDecimal handleRate;

    // 按品种统计
    @Schema(description = "按品种入库统计")
    private List<VarietyInboundStat> inboundByVariety;

    @Schema(description = "按品种出库统计")
    private List<VarietyOutboundStat> outboundByVariety;

    // 按日期统计
    @Schema(description = "每日入库统计")
    private List<DailyStat> dailyInbound;

    @Schema(description = "每日出库统计")
    private List<DailyStat> dailyOutbound;

    @Data
    @Schema(description = "按品种入库统计")
    public static class VarietyInboundStat {
        @Schema(description = "品种ID")
        private Long grainVarietyId;

        @Schema(description = "品种名称")
        private String varietyName;

        @Schema(description = "入库数量(吨)")
        private BigDecimal quantity;

        @Schema(description = "入库次数")
        private Integer count;

        @Schema(description = "占比(%)")
        private BigDecimal percentage;
    }

    @Data
    @Schema(description = "按品种出库统计")
    public static class VarietyOutboundStat {
        @Schema(description = "品种ID")
        private Long grainVarietyId;

        @Schema(description = "品种名称")
        private String varietyName;

        @Schema(description = "出库数量(吨)")
        private BigDecimal quantity;

        @Schema(description = "出库次数")
        private Integer count;

        @Schema(description = "占比(%)")
        private BigDecimal percentage;
    }

    @Data
    @Schema(description = "每日统计")
    public static class DailyStat {
        @Schema(description = "日期(日)")
        private Integer day;

        @Schema(description = "数量(吨)")
        private BigDecimal quantity;

        @Schema(description = "次数")
        private Integer count;
    }
}