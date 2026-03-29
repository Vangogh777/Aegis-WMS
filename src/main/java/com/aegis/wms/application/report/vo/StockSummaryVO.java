package com.aegis.wms.application.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存汇总VO
 */
@Data
@Schema(description = "库存汇总")
public class StockSummaryVO {

    @Schema(description = "库存总量(吨)")
    private BigDecimal totalQuantity;

    @Schema(description = "货位数量")
    private Integer positionCount;

    @Schema(description = "占用货位数")
    private Integer occupiedPositionCount;

    @Schema(description = "按品种汇总")
    private List<VarietySummary> byVariety;

    @Schema(description = "按仓房汇总")
    private List<BinSummary> byBin;

    @Data
    @Schema(description = "按品种汇总")
    public static class VarietySummary {
        @Schema(description = "品种ID")
        private Long grainVarietyId;

        @Schema(description = "品种编码")
        private String varietyCode;

        @Schema(description = "品种名称")
        private String varietyName;

        @Schema(description = "库存数量(吨)")
        private BigDecimal quantity;

        @Schema(description = "占比(%)")
        private BigDecimal percentage;
    }

    @Data
    @Schema(description = "按仓房汇总")
    public static class BinSummary {
        @Schema(description = "仓房ID")
        private Long binId;

        @Schema(description = "仓房编码")
        private String binCode;

        @Schema(description = "仓房名称")
        private String binName;

        @Schema(description = "仓容(吨)")
        private BigDecimal capacity;

        @Schema(description = "库存数量(吨)")
        private BigDecimal quantity;

        @Schema(description = "使用率(%)")
        private BigDecimal usageRate;

        @Schema(description = "品种名称")
        private String grainVarietyName;
    }
}