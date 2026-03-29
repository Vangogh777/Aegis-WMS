package com.aegis.wms.interfaces.controller.inventory;

import com.aegis.wms.application.inventory.dto.GrainVarietyDTO;
import com.aegis.wms.application.inventory.service.GrainVarietyService;
import com.aegis.wms.application.inventory.vo.GrainVarietyVO;
import com.aegis.wms.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 粮食品种控制器
 */
@Tag(name = "粮食品种管理", description = "粮食品种字典接口")
@RestController
@RequestMapping("/api/inventory/grain-variety")
@RequiredArgsConstructor
public class GrainVarietyController {

    private final GrainVarietyService grainVarietyService;

    @Operation(summary = "创建粮食品种")
    @PostMapping
    public Result<Long> create(@RequestBody GrainVarietyDTO dto) {
        return Result.success(grainVarietyService.create(dto));
    }

    @Operation(summary = "更新粮食品种")
    @PutMapping
    public Result<Boolean> update(@RequestBody GrainVarietyDTO dto) {
        return Result.success(grainVarietyService.update(dto));
    }

    @Operation(summary = "删除粮食品种")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(grainVarietyService.delete(id));
    }

    @Operation(summary = "查询所有粮食品种")
    @GetMapping("/list")
    public Result<List<GrainVarietyVO>> listAll() {
        return Result.success(grainVarietyService.listAll());
    }

    @Operation(summary = "根据ID查询粮食品种")
    @GetMapping("/{id}")
    public Result<GrainVarietyVO> getById(@PathVariable Long id) {
        return Result.success(grainVarietyService.getById(id));
    }
}