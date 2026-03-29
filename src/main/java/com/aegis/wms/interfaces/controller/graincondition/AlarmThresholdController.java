package com.aegis.wms.interfaces.controller.graincondition;

import com.aegis.wms.application.graincondition.dto.AlarmThresholdDTO;
import com.aegis.wms.application.graincondition.service.AlarmThresholdService;
import com.aegis.wms.application.graincondition.vo.AlarmThresholdVO;
import com.aegis.wms.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报警阈值控制器
 */
@Tag(name = "报警阈值", description = "报警阈值配置接口")
@RestController
@RequestMapping("/api/graincondition/threshold")
@RequiredArgsConstructor
public class AlarmThresholdController {

    private final AlarmThresholdService alarmThresholdService;

    @Operation(summary = "创建报警阈值")
    @PostMapping
    public Result<Long> create(@RequestBody AlarmThresholdDTO dto) {
        return Result.success(alarmThresholdService.create(dto));
    }

    @Operation(summary = "更新报警阈值")
    @PutMapping
    public Result<Boolean> update(@RequestBody AlarmThresholdDTO dto) {
        return Result.success(alarmThresholdService.update(dto));
    }

    @Operation(summary = "删除报警阈值")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(alarmThresholdService.delete(id));
    }

    @Operation(summary = "查询报警阈值")
    @GetMapping("/{id}")
    public Result<AlarmThresholdVO> getById(@PathVariable Long id) {
        return Result.success(alarmThresholdService.getById(id));
    }

    @Operation(summary = "查询所有启用的阈值")
    @GetMapping("/list")
    public Result<List<AlarmThresholdVO>> listAllEnabled() {
        return Result.success(alarmThresholdService.listAllEnabled());
    }
}