package com.aegis.wms.interfaces.controller.operation;

import com.aegis.wms.application.operation.service.OperationStatisticsService;
import com.aegis.wms.application.operation.vo.OperationStatisticsVO;
import com.aegis.wms.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 作业统计控制器
 */
@Tag(name = "作业统计", description = "作业数据统计接口")
@RestController
@RequestMapping("/api/operation/statistics")
@RequiredArgsConstructor
public class OperationStatisticsController {

    private final OperationStatisticsService operationStatisticsService;

    @Operation(summary = "按库区统计作业", description = "按库区和时间范围统计作业次数、时长、耗电量等")
    @GetMapping("/warehouse")
    public Result<OperationStatisticsVO> statisticsByWarehouse(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(operationStatisticsService.statisticsByWarehouse(warehouseId, startDate, endDate));
    }

    @Operation(summary = "按仓房统计作业")
    @GetMapping("/bin")
    public Result<OperationStatisticsVO> statisticsByBin(
            @RequestParam Long binId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(operationStatisticsService.statisticsByBin(binId, startDate, endDate));
    }

    @Operation(summary = "按作业类型统计")
    @GetMapping("/type")
    public Result<OperationStatisticsVO> statisticsByType(
            @RequestParam String operationType,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(operationStatisticsService.statisticsByType(operationType, warehouseId, startDate, endDate));
    }

    @Operation(summary = "按类型汇总统计", description = "返回各作业类型的统计数据")
    @GetMapping("/summary/by-type")
    public Result<List<OperationStatisticsVO.TypeStatistics>> summaryByType(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(operationStatisticsService.summaryByType(warehouseId, startDate, endDate));
    }
}
