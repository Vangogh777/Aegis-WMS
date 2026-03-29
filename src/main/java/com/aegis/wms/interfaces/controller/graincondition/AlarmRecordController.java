package com.aegis.wms.interfaces.controller.graincondition;

import com.aegis.wms.application.graincondition.dto.AlarmHandleDTO;
import com.aegis.wms.application.graincondition.service.AlarmRecordService;
import com.aegis.wms.application.graincondition.vo.AlarmRecordVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报警记录控制器
 */
@Tag(name = "报警记录", description = "报警记录接口")
@RestController
@RequestMapping("/api/graincondition/alarm")
@RequiredArgsConstructor
public class AlarmRecordController {

    private final AlarmRecordService alarmRecordService;

    @Operation(summary = "分页查询报警记录")
    @GetMapping("/page")
    public Result<PageResult<AlarmRecordVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId,
            @RequestParam(required = false) Integer status) {
        Page<AlarmRecordVO> page = new Page<>(current, size);
        return Result.success(alarmRecordService.page(page, warehouseId, binId, status));
    }

    @Operation(summary = "查询报警记录")
    @GetMapping("/{id}")
    public Result<AlarmRecordVO> getById(@PathVariable Long id) {
        return Result.success(alarmRecordService.getById(id));
    }

    @Operation(summary = "处理报警")
    @PutMapping("/handle")
    public Result<Boolean> handle(@RequestBody AlarmHandleDTO dto) {
        return Result.success(alarmRecordService.handle(dto));
    }

    @Operation(summary = "查询未处理报警数量")
    @GetMapping("/count/unhandled")
    public Result<Long> countUnhandled(@RequestParam(required = false) Long warehouseId) {
        return Result.success(alarmRecordService.countUnhandled(warehouseId));
    }

    @Operation(summary = "查询未处理报警列表")
    @GetMapping("/unhandled")
    public Result<List<AlarmRecordVO>> listUnhandled(@RequestParam(required = false) Long warehouseId) {
        return Result.success(alarmRecordService.listUnhandled(warehouseId));
    }
}