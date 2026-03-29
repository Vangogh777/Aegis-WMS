package com.aegis.wms.interfaces.controller.operation;

import com.aegis.wms.application.operation.dto.OperationSchemeDTO;
import com.aegis.wms.application.operation.service.OperationSchemeService;
import com.aegis.wms.application.operation.vo.OperationSchemeVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业方案控制器
 */
@Tag(name = "作业方案", description = "作业方案配置接口")
@RestController
@RequestMapping("/api/operation/scheme")
@RequiredArgsConstructor
public class OperationSchemeController {

    private final OperationSchemeService operationSchemeService;

    @Operation(summary = "创建作业方案")
    @PostMapping
    public Result<Long> create(@RequestBody OperationSchemeDTO dto) {
        return Result.success(operationSchemeService.create(dto));
    }

    @Operation(summary = "更新作业方案")
    @PutMapping
    public Result<Boolean> update(@RequestBody OperationSchemeDTO dto) {
        return Result.success(operationSchemeService.update(dto));
    }

    @Operation(summary = "删除作业方案")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(operationSchemeService.delete(id));
    }

    @Operation(summary = "查询作业方案")
    @GetMapping("/{id}")
    public Result<OperationSchemeVO> getById(@PathVariable Long id) {
        return Result.success(operationSchemeService.getById(id));
    }

    @Operation(summary = "根据编码查询作业方案")
    @GetMapping("/code/{code}")
    public Result<OperationSchemeVO> getByCode(@PathVariable String code) {
        return Result.success(operationSchemeService.getByCode(code));
    }

    @Operation(summary = "查询所有启用的作业方案")
    @GetMapping("/list")
    public Result<List<OperationSchemeVO>> listAllEnabled() {
        return Result.success(operationSchemeService.listAllEnabled());
    }

    @Operation(summary = "根据作业类型查询方案列表")
    @GetMapping("/type/{operationType}")
    public Result<List<OperationSchemeVO>> listByType(@PathVariable String operationType) {
        return Result.success(operationSchemeService.listByType(operationType));
    }

    @Operation(summary = "分页查询作业方案")
    @GetMapping("/page")
    public Result<PageResult<OperationSchemeVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String schemeName,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) Integer status) {
        Page<OperationSchemeVO> page = new Page<>(current, size);
        return Result.success(operationSchemeService.page(page, schemeName, operationType, status));
    }
}
