package com.aegis.wms.application.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 出入库统计VO
 */
@Data
@Schema(description = "出入库统计")
public class InboundOutboundStatisticsVO {

    @Schema(description = "本月入库数量(吨)")
    private BigDecimal monthlyInboundQuantity;

    @Schema(description = "本月入库次数")
    private Integer monthlyInboundCount;

    @Schema(description = "本月出库数量(吨)")
    private BigDecimal monthlyOutboundQuantity;

    @Schema(description = "本月出库次数")
    private Integer monthlyOutboundCount;

    @Schema(description = "本年入库数量(吨)")
    private BigDecimal yearlyInboundQuantity;

    @Schema(description = "本年入库次数")
    private Integer yearlyInboundCount;

    @Schema(description = "本年出库数量(吨)")
    private BigDecimal yearlyOutboundQuantity;

    @Schema(description = "本年出库次数")
    private Integer yearlyOutboundCount;

    @Schema(description = "按品种统计入库")
    private List<VarietyInbound> inboundByVariety;

    @Schema(description = "按品种统计出库")
    private List<VarietyOutbound> outboundByVariety;

    @Schema(description = "按仓房统计入库")
    private List<BinInbound> inboundByBin;

    @Schema(description = "按仓房统计出库")
    private List<BinOutbound> outboundByBin;

    @Data
    @Schema(description = "按品种统计入库")
    public static class VarietyInbound {
        @Schema(description = "品种ID")
        private Long grainVarietyId;

        @Schema(description = "品种名称")
        private String varietyName;

        @Schema(description = "入库数量(吨)")
        private BigDecimal quantity;

        @Schema(description = "入库次数")
        private Integer count;
    }

    @Data
    @Schema(description = "按品种统计出库")
    public static class VarietyOutbound {
        @Schema(description = "品种ID")
        private Long grainVarietyId;

        @Schema(description = "品种名称")
        private String varietyName;

        @Schema(description = "出库数量(吨)")
        private BigDecimal quantity;

        @Schema(description = "出库次数")
        private Integer count;
    }

    @Data
    @Schema(description = "按仓房统计入库")
    public static class BinInbound {
        @Schema(description = "仓房ID")
        private Long binId;

        @Schema(description = "仓房编码")
        private String binCode;

        @Schema(description = "仓房名称")
        private String binName;

        @Schema(description = "入库数量(吨)")
        private BigDecimal quantity;

        @Schema(description = "入库次数")
        private Integer count;
    }

    @Data
    @Schema(description = "按仓房统计出库")
    public static class BinOutbound {
        @Schema(description = "仓房ID")
        private Long binId;

        @Schema(description = "仓房编码")
        private String binCode;

        @Schema(description = "仓房名称")
        private String binName;

        @Schema(description = "出库数量(吨)")
        private BigDecimal quantity;

        @Schema(description = "出库次数")
        private Integer count;
    }
}