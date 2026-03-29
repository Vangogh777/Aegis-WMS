package com.aegis.wms.application.graincondition.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 粮情趋势数据VO
 * 用于三温比对折线图
 */
@Data
public class GrainConditionTrendVO {

    /**
     * 日期
     */
    private LocalDate recordDate;

    /**
     * 仓外温度(℃)
     */
    private BigDecimal outerTemp;

    /**
     * 仓内温度(℃)
     */
    private BigDecimal innerTemp;

    /**
     * 平均粮温(℃)
     */
    private BigDecimal avgGrainTemp;

    /**
     * 最高粮温(℃)
     */
    private BigDecimal maxGrainTemp;

    /**
     * 最低粮温(℃)
     */
    private BigDecimal minGrainTemp;

    /**
     * 仓外湿度(%)
     */
    private BigDecimal outerHumidity;

    /**
     * 仓内湿度(%)
     */
    private BigDecimal innerHumidity;
}