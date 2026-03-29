package com.aegis.wms.interfaces.controller.report;

import com.aegis.wms.application.report.service.ReportService;
import com.aegis.wms.application.report.vo.*;
import com.aegis.wms.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 报表控制器
 */
@Tag(name = "数据报表", description = "数据统计与报表接口")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "库区概览", description = "获取库区整体运营数据概览")
    @GetMapping("/overview")
    public Result<WarehouseOverviewVO> getWarehouseOverview(
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(reportService.getWarehouseOverview(warehouseId));
    }

    @Operation(summary = "库存汇总", description = "按品种、仓房汇总库存数据")
    @GetMapping("/stock")
    public Result<StockSummaryVO> getStockSummary(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId) {
        return Result.success(reportService.getStockSummary(warehouseId, binId));
    }

    @Operation(summary = "粮情汇总", description = "各仓房最新粮情状态汇总")
    @GetMapping("/grain-condition")
    public Result<GrainConditionSummaryVO> getGrainConditionSummary(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId) {
        return Result.success(reportService.getGrainConditionSummary(warehouseId, binId));
    }

    @Operation(summary = "作业汇总", description = "本月出入库、粮情检测、报警统计")
    @GetMapping("/operation")
    public Result<OperationSummaryVO> getOperationSummary(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return Result.success(reportService.getOperationSummary(warehouseId, year, month));
    }

    @Operation(summary = "报警统计", description = "按级别、类型、仓房统计报警数据")
    @GetMapping("/alarm")
    public Result<AlarmStatisticsVO> getAlarmStatistics(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(reportService.getAlarmStatistics(warehouseId, startDate, endDate));
    }

    @Operation(summary = "出入库统计", description = "本月/本年出入库数据统计")
    @GetMapping("/inbound-outbound")
    public Result<InboundOutboundStatisticsVO> getInboundOutboundStatistics(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return Result.success(reportService.getInboundOutboundStatistics(warehouseId, year, month));
    }

    @Operation(summary = "日报表", description = "获取指定日期的运营数据汇总")
    @GetMapping("/daily")
    public Result<DailyReportVO> getDailyReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(reportService.getDailyReport(date, warehouseId));
    }

    @Operation(summary = "月报表", description = "获取指定月份的运营数据汇总")
    @GetMapping("/monthly")
    public Result<MonthlyReportVO> getMonthlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(reportService.getMonthlyReport(year, month, warehouseId));
    }
}
