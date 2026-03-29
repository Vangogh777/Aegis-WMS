package com.aegis.wms.interfaces.controller.operation;

import com.aegis.wms.application.operation.dto.OperationDetailDTO;
import com.aegis.wms.application.operation.service.OperationDetailService;
import com.aegis.wms.application.operation.vo.OperationDetailVO;
import com.aegis.wms.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业明细控制器
 */
@Tag(name = "作业明细", description = "作业启停明细管理接口")
@RestController
@RequestMapping("/api/operation/detail")
@RequiredArgsConstructor
public class OperationDetailController {

    private final OperationDetailService operationDetailService;

    @Operation(summary = "启动作业明细", description = "开始一次启停记录")
    @PostMapping("/start")
    public Result<Long> start(@RequestBody OperationDetailDTO dto) {
        return Result.success(operationDetailService.start(dto));
    }

    @Operation(summary = "停止作业明细", description = "结束一次启停记录，计算时长和耗电量")
    @PostMapping("/stop")
    public Result<Boolean> stop(@RequestBody OperationDetailDTO dto) {
        return Result.success(operationDetailService.stop(dto));
    }

    @Operation(summary = "取消作业明细")
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancel(@PathVariable Long id) {
        return Result.success(operationDetailService.cancel(id));
    }

    @Operation(summary = "更新作业明细")
    @PutMapping
    public Result<Boolean> update(@RequestBody OperationDetailDTO dto) {
        return Result.success(operationDetailService.update(dto));
    }

    @Operation(summary = "查询作业明细")
    @GetMapping("/{id}")
    public Result<OperationDetailVO> getById(@PathVariable Long id) {
        return Result.success(operationDetailService.getById(id));
    }

    @Operation(summary = "查询作业记录的所有明细")
    @GetMapping("/record/{recordId}")
    public Result<List<OperationDetailVO>> listByRecordId(@PathVariable Long recordId) {
        return Result.success(operationDetailService.listByRecordId(recordId));
    }

    @Operation(summary = "获取作业记录当前进行中的明细")
    @GetMapping("/in-progress/{recordId}")
    public Result<OperationDetailVO> getInProgressDetail(@PathVariable Long recordId) {
        return Result.success(operationDetailService.getInProgressDetail(recordId));
    }
}