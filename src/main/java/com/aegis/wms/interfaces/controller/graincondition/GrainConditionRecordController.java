package com.aegis.wms.interfaces.controller.graincondition;

import com.aegis.wms.application.graincondition.dto.GrainConditionRecordDTO;
import com.aegis.wms.application.graincondition.service.GrainConditionRecordService;
import com.aegis.wms.application.graincondition.vo.GrainConditionRecordVO;
import com.aegis.wms.application.graincondition.vo.GrainConditionTrendVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 粮情记录控制器
 */
@Tag(name = "粮情检测", description = "粮情记录接口")
@RestController
@RequestMapping("/api/graincondition/record")
@RequiredArgsConstructor
public class GrainConditionRecordController {

    private final GrainConditionRecordService grainConditionRecordService;

    @Operation(summary = "创建粮情记录", description = "录入粮情后发送Kafka消息异步触发报警检测")
    @PostMapping
    public Result<Long> create(@RequestBody GrainConditionRecordDTO dto) {
        return Result.success(grainConditionRecordService.create(dto));
    }

    @Operation(summary = "更新粮情记录")
    @PutMapping
    public Result<Boolean> update(@RequestBody GrainConditionRecordDTO dto) {
        return Result.success(grainConditionRecordService.update(dto));
    }

    @Operation(summary = "删除粮情记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(grainConditionRecordService.delete(id));
    }

    @Operation(summary = "查询粮情记录")
    @GetMapping("/{id}")
    public Result<GrainConditionRecordVO> getById(@PathVariable Long id) {
        return Result.success(grainConditionRecordService.getById(id));
    }

    @Operation(summary = "分页查询粮情记录")
    @GetMapping("/page")
    public Result<PageResult<GrainConditionRecordVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<GrainConditionRecordVO> page = new Page<>(current, size);
        return Result.success(grainConditionRecordService.page(page, warehouseId, binId, startDate, endDate));
    }

    @Operation(summary = "查询粮情趋势", description = "用于三温比对折线图")
    @GetMapping("/trend/{binId}")
    public Result<List<GrainConditionTrendVO>> getTrend(
            @PathVariable Long binId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(grainConditionRecordService.getTrend(binId, startDate, endDate));
    }

    @Operation(summary = "查询仓房最新粮情")
    @GetMapping("/latest/{binId}")
    public Result<GrainConditionRecordVO> getLatest(@PathVariable Long binId) {
        return Result.success(grainConditionRecordService.getLatestByBinId(binId));
    }
}