package com.aegis.wms.application.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 日报表VO
 */
@Data
@Schema(description = "日报表")
public class DailyReportVO {

    @Schema(description = "报表日期")
    private LocalDate reportDate;

    @Schema(description = "库区ID")
    private Long warehouseId;

    @Schema(description = "库区名称")
    private String warehouseName;

    // 库存信息
    @Schema(description = "当日库存总量(吨)")
    private BigDecimal totalStock;

    @Schema(description = "当日库存变动(吨)")
    private BigDecimal stockChange;

    // 入库信息
    @Schema(description = "当日入库次数")
    private Integer inboundCount;

    @Schema(description = "当日入库数量(吨)")
    private BigDecimal inboundQuantity;

    // 出库信息
    @Schema(description = "当日出库次数")
    private Integer outboundCount;

    @Schema(description = "当日出库数量(吨)")
    private BigDecimal outboundQuantity;

    // 粮情信息
    @Schema(description = "当日检测仓房数")
    private Integer detectedBinCount;

    @Schema(description = "平均粮温(℃)")
    private BigDecimal avgGrainTemp;

    @Schema(description = "最高粮温(℃)")
    private BigDecimal maxGrainTemp;

    @Schema(description = "最低粮温(℃)")
    private BigDecimal minGrainTemp;

    // 报警信息
    @Schema(description = "当日报警次数")
    private Integer alarmCount;

    @Schema(description = "当日处理报警次数")
    private Integer handledAlarmCount;

    // 仓房明细
    @Schema(description = "各仓房明细")
    private List<BinDailyDetail> binDetails;

    @Data
    @Schema(description = "仓房日报明细")
    public static class BinDailyDetail {
        @Schema(description = "仓房ID")
        private Long binId;

        @Schema(description = "仓房编码")
        private String binCode;

        @Schema(description = "仓房名称")
        private String binName;

        @Schema(description = "仓容(吨)")
        private BigDecimal capacity;

        @Schema(description = "库存数量(吨)")
        private BigDecimal stockQuantity;

        @Schema(description = "使用率(%)")
        private BigDecimal usageRate;

        @Schema(description = "当日入库数量(吨)")
        private BigDecimal inboundQuantity;

        @Schema(description = "当日出库数量(吨)")
        private BigDecimal outboundQuantity;

        @Schema(description = "最高粮温(℃)")
        private BigDecimal maxGrainTemp;

        @Schema(description = "最低粮温(℃)")
        private BigDecimal minGrainTemp;

        @Schema(description = "平均粮温(℃)")
        private BigDecimal avgGrainTemp;

        @Schema(description = "报警次数")
        private Integer alarmCount;

        @Schema(description = "状态: normal-正常 warning-预警 alarm-报警")
        private String status;
    }
}