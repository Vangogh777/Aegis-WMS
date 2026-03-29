package com.aegis.wms.application.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库区概览VO
 */
@Data
@Schema(description = "库区概览")
public class WarehouseOverviewVO {

    @Schema(description = "库区ID")
    private Long warehouseId;

    @Schema(description = "库区编码")
    private String warehouseCode;

    @Schema(description = "库区名称")
    private String warehouseName;

    @Schema(description = "总设计容量(吨)")
    private BigDecimal totalCapacity;

    @Schema(description = "仓房数量")
    private Integer binCount;

    @Schema(description = "货位数量")
    private Integer positionCount;

    @Schema(description = "库存总量(吨)")
    private BigDecimal totalStock;

    @Schema(description = "库容使用率(%)")
    private BigDecimal usageRate;

    @Schema(description = "空仓房数量")
    private Integer emptyBinCount;

    @Schema(description = "满仓房数量")
    private Integer fullBinCount;
}