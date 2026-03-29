package com.aegis.wms.application.inventory.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 粮食品种VO
 */
@Data
public class GrainVarietyVO {

    private Long id;

    private String varietyCode;

    private String varietyName;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;
}