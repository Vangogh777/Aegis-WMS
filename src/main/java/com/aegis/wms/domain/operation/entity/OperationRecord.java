package com.aegis.wms.domain.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业记录实体
 * 作业主记录，状态机管控
 * 状态流转: 待执行(0) -> 作业中(1) -> 已完成(2) / 已取消(3)
 */
@Data
@TableName("warehouse_operation_record")
public class OperationRecord {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 记录编号
     */
    private String recordNo;

    /**
     * 关联方案ID(可为空,表示临时作业)
     */
    private Long schemeId;

    /**
     * 库区ID
     */
    private Long warehouseId;

    /**
     * 仓房ID
     */
    private Long binId;

    /**
     * 作业类型: ventilation-通风/aeration-气调/temperature-控温/fumigation-熏蒸
     */
    private String operationType;

    /**
     * 状态: 0-待执行 1-作业中 2-已完成 3-已取消
     */
    private Integer status;

    /**
     * 首次开始时间
     */
    private LocalDateTime startTime;

    /**
     * 最后结束时间
     */
    private LocalDateTime endTime;

    /**
     * 累计作业时长(分钟)
     */
    private Integer totalDurationMinutes;

    /**
     * 累计耗电量(度)
     */
    private BigDecimal totalPower;

    /**
     * 启停次数
     */
    private Integer detailCount;

    /**
     * 起始平均粮温(℃)
     */
    private BigDecimal startAvgTemp;

    /**
     * 起始水分(%)
     */
    private BigDecimal startMoisture;

    /**
     * 结束平均粮温(℃)
     */
    private BigDecimal endAvgTemp;

    /**
     * 结束水分(%)
     */
    private BigDecimal endMoisture;

    /**
     * 温度变化(℃)
     */
    private BigDecimal tempDrop;

    /**
     * 水分变化(%)
     */
    private BigDecimal moistureDrop;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 备注
     */
    private String remark;

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

    // ==================== 状态常量 ====================

    /**
     * 待执行
     */
    public static final int STATUS_PENDING = 0;

    /**
     * 作业中
     */
    public static final int STATUS_IN_PROGRESS = 1;

    /**
     * 已完成
     */
    public static final int STATUS_COMPLETED = 2;

    /**
     * 已取消
     */
    public static final int STATUS_CANCELLED = 3;

    // ==================== 作业类型常量 ====================

    /**
     * 通风作业
     */
    public static final String TYPE_VENTILATION = "ventilation";

    /**
     * 气调作业
     */
    public static final String TYPE_AERATION = "aeration";

    /**
     * 控温作业
     */
    public static final String TYPE_TEMPERATURE = "temperature";

    /**
     * 熏蒸作业
     */
    public static final String TYPE_FUMIGATION = "fumigation";
}