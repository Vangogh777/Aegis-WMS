package com.aegis.wms.domain.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业方案实体
 * 可复用的作业模板，包含通风、气调、控温、熏蒸等作业类型
 */
@Data
@TableName("warehouse_operation_scheme")
public class OperationScheme {

    /**
     * 方案ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 方案编码
     */
    private String schemeCode;

    /**
     * 方案名称
     */
    private String schemeName;

    /**
     * 作业类型: ventilation-通风/aeration-气调/temperature-控温/fumigation-熏蒸
     */
    private String operationType;

    /**
     * 方案配置参数(JSON格式,不同作业类型差异化参数)
     * 通风-风机功率/风道类型; 气调-目标气体浓度等
     */
    private String configParams;

    /**
     * 温度阈值(℃)
     */
    private BigDecimal tempThreshold;

    /**
     * 湿度阈值(%)
     */
    private BigDecimal humidityThreshold;

    /**
     * 最长作业时长(分钟)
     */
    private Integer durationMax;

    /**
     * 方案说明
     */
    private String description;

    /**
     * 状态: 1-启用 0-禁用
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建人姓名
     */
    private String createByName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记: 0-正常 1-已删除
     */
    @TableLogic
    private Integer deleted;
}