package com.aegis.wms.application.operation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 作业方案DTO
 */
@Data
@Schema(description = "作业方案DTO")
public class OperationSchemeDTO {

    @Schema(description = "方案ID(更新时必填)")
    private Long id;

    @Schema(description = "方案编码(创建时必填)")
    private String schemeCode;

    @Schema(description = "方案名称")
    private String schemeName;

    @Schema(description = "作业类型: ventilation-通风/aeration-气调/temperature-控温/fumigation-熏蒸")
    private String operationType;

    @Schema(description = "方案配置参数(JSON格式)")
    private String configParams;

    @Schema(description = "温度阈值(℃)")
    private BigDecimal tempThreshold;

    @Schema(description = "湿度阈值(%)")
    private BigDecimal humidityThreshold;

    @Schema(description = "最长作业时长(分钟)")
    private Integer durationMax;

    @Schema(description = "方案说明")
    private String description;

    @Schema(description = "状态: 1-启用 0-禁用")
    private Integer status;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "创建人姓名")
    private String createByName;
}