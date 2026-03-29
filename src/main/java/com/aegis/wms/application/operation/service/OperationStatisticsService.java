package com.aegis.wms.application.operation.service;

import com.aegis.wms.application.operation.vo.OperationStatisticsVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 作业统计服务接口
 */
public interface OperationStatisticsService {

    /**
     * 按库区统计作业
     *
     * @param warehouseId 库区ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    OperationStatisticsVO statisticsByWarehouse(Long warehouseId, LocalDate startDate, LocalDate endDate);

    /**
     * 按仓房统计作业
     *
     * @param binId 仓房ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    OperationStatisticsVO statisticsByBin(Long binId, LocalDate startDate, LocalDate endDate);

    /**
     * 按作业类型统计
     *
     * @param operationType 作业类型
     * @param warehouseId 库区ID(可选)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    OperationStatisticsVO statisticsByType(String operationType, Long warehouseId, LocalDate startDate, LocalDate endDate);

    /**
     * 汇总统计(按类型分组)
     *
     * @param warehouseId 库区ID(可选)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 按类型分组的统计列表
     */
    List<OperationStatisticsVO.TypeStatistics> summaryByType(Long warehouseId, LocalDate startDate, LocalDate endDate);

    /**
     * 计算作业记录的汇总数据
     *
     * @param recordId 记录ID
     * @return 汇总结果
     */
    OperationStatisticsVO calculateRecordSummary(Long recordId);
}