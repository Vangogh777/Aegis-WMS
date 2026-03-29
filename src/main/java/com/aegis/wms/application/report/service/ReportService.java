package com.aegis.wms.application.report.service;

import com.aegis.wms.application.report.vo.*;

import java.time.LocalDate;

/**
 * 报表服务接口
 */
public interface ReportService {

    /**
     * 获取库区概览
     *
     * @param warehouseId 库区ID，为null时查询所有库区汇总
     * @return 库区概览
     */
    WarehouseOverviewVO getWarehouseOverview(Long warehouseId);

    /**
     * 获取库存汇总
     *
     * @param warehouseId 库区ID，为null时查询所有
     * @param binId       仓房ID，为null时查询所有
     * @return 库存汇总
     */
    StockSummaryVO getStockSummary(Long warehouseId, Long binId);

    /**
     * 获取粮情汇总
     *
     * @param warehouseId 库区ID，为null时查询所有
     * @param binId       仓房ID，为null时查询所有
     * @return 粮情汇总
     */
    GrainConditionSummaryVO getGrainConditionSummary(Long warehouseId, Long binId);

    /**
     * 获取作业汇总
     *
     * @param warehouseId 库区ID，为null时查询所有
     * @param year        年份，为null时使用当前年
     * @param month       月份，为null时使用当前月
     * @return 作业汇总
     */
    OperationSummaryVO getOperationSummary(Long warehouseId, Integer year, Integer month);

    /**
     * 获取报警统计
     *
     * @param warehouseId 库区ID，为null时查询所有
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 报警统计
     */
    AlarmStatisticsVO getAlarmStatistics(Long warehouseId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取出入库统计
     *
     * @param warehouseId 库区ID，为null时查询所有
     * @param year        年份，为null时使用当前年
     * @param month       月份，为null时使用当前月
     * @return 出入库统计
     */
    InboundOutboundStatisticsVO getInboundOutboundStatistics(Long warehouseId, Integer year, Integer month);

    /**
     * 获取日报表
     *
     * @param date        日期，为null时使用当前日期
     * @param warehouseId 库区ID，为null时查询所有
     * @return 日报表
     */
    DailyReportVO getDailyReport(LocalDate date, Long warehouseId);

    /**
     * 获取月报表
     *
     * @param year        年份，为null时使用当前年
     * @param month       月份，为null时使用当前月
     * @param warehouseId 库区ID，为null时查询所有
     * @return 月报表
     */
    MonthlyReportVO getMonthlyReport(Integer year, Integer month, Long warehouseId);
}