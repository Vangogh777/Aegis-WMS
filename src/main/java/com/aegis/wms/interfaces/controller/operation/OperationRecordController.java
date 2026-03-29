package com.aegis.wms.interfaces.controller.operation;

import com.aegis.wms.application.operation.dto.OperationRecordDTO;
import com.aegis.wms.application.operation.service.OperationRecordService;
import com.aegis.wms.application.operation.vo.OperationRecordVO;
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
 * 作业记录控制器
 */
@Tag(name = "作业记录", description = "作业记录管理接口")
@RestController
@RequestMapping("/api/operation/record")
@RequiredArgsConstructor
public class OperationRecordController {

    private final OperationRecordService operationRecordService;

    @Operation(summary = "创建作业记录", description = "创建待执行状态的作业记录")
    @PostMapping
    public Result<Long> create(@RequestBody OperationRecordDTO dto) {
        return Result.success(operationRecordService.create(dto));
    }

    @Operation(summary = "更新作业记录")
    @PutMapping
    public Result<Boolean> update(@RequestBody OperationRecordDTO dto) {
        return Result.success(operationRecordService.update(dto));
    }

    @Operation(summary = "删除作业记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(operationRecordService.delete(id));
    }

    @Operation(summary = "查询作业记录", description = "包含作业明细列表")
    @GetMapping("/{id}")
    public Result<OperationRecordVO> getById(@PathVariable Long id) {
        return Result.success(operationRecordService.getById(id));
    }

    @Operation(summary = "根据编号查询作业记录")
    @GetMapping("/no/{recordNo}")
    public Result<OperationRecordVO> getByRecordNo(@PathVariable String recordNo) {
        return Result.success(operationRecordService.getByRecordNo(recordNo));
    }

    @Operation(summary = "启动作业", description = "状态流转: 待执行 -> 作业中")
    @PostMapping("/{id}/start")
    public Result<Boolean> startOperation(
            @PathVariable Long id,
            @RequestParam Long operatorId,
            @RequestParam String operatorName) {
        return Result.success(operationRecordService.startOperation(id, operatorId, operatorName));
    }

    @Operation(summary = "完成作业", description = "状态流转: 作业中 -> 已完成")
    @PostMapping("/{id}/complete")
    public Result<Boolean> completeOperation(
            @PathVariable Long id,
            @RequestParam Long operatorId,
            @RequestParam String operatorName) {
        return Result.success(operationRecordService.completeOperation(id, operatorId, operatorName));
    }

    @Operation(summary = "取消作业", description = "状态流转: 待执行/作业中 -> 已取消")
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelOperation(
            @PathVariable Long id,
            @RequestParam Long operatorId,
            @RequestParam String operatorName,
            @RequestParam(required = false) String cancelReason) {
        return Result.success(operationRecordService.cancelOperation(id, operatorId, operatorName, cancelReason));
    }

    @Operation(summary = "汇总作业记录", description = "更新作业汇总数据(时长、耗电量、温度变化)")
    @PostMapping("/{id}/summarize")
    public Result<Boolean> summarize(@PathVariable Long id) {
        return Result.success(operationRecordService.summarize(id));
    }

    @Operation(summary = "分页查询作业记录")
    @GetMapping("/page")
    public Result<PageResult<OperationRecordVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<OperationRecordVO> page = new Page<>(current, size);
        return Result.success(operationRecordService.page(page, warehouseId, binId, operationType, status, startDate, endDate));
    }

    @Operation(summary = "查询仓房的作业记录")
    @GetMapping("/bin/{binId}")
    public Result<List<OperationRecordVO>> listByBinId(@PathVariable Long binId) {
        return Result.success(operationRecordService.listByBinId(binId));
    }
}
