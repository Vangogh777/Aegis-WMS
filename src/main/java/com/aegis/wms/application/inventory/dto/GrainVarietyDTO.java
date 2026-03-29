package com.aegis.wms.application.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 粮食品种DTO
 */
@Data
public class GrainVarietyDTO {

    private Long id;

    private String varietyCode;

    private String varietyName;

    private Integer sortOrder;

    private Integer status;
}