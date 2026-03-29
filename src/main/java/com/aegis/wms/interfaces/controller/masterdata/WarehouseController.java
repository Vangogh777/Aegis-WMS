package com.aegis.wms.interfaces.controller.masterdata;

import com.aegis.wms.application.masterdata.dto.WarehouseDTO;
import com.aegis.wms.application.masterdata.vo.WarehouseVO;
import com.aegis.wms.application.masterdata.service.WarehouseService;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库区控制器
 * 提供库区管理的REST API
 */
@Tag(name = "库区管理", description = "库区增删改查接口")
@RestController
@RequestMapping("/api/masterdata/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * 创建库区
     */
    @Operation(summary = "创建库区", description = "新增一个库区")
    @PostMapping
    public Result<Long> create(@RequestBody WarehouseDTO dto) {
        Long id = warehouseService.create(dto);
        return Result.success(id);
    }

    /**
     * 更新库区
     */
    @Operation(summary = "更新库区", description = "修改库区信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody WarehouseDTO dto) {
        Boolean result = warehouseService.update(dto);
        return Result.success(result);
    }

    /**
     * 删除库区
     */
    @Operation(summary = "删除库区", description = "逻辑删除库区")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@Parameter(description = "库区ID") @PathVariable Long id) {
        Boolean result = warehouseService.delete(id);
        return Result.success(result);
    }

    /**
     * 根据ID查询库区
     */
    @Operation(summary = "查询库区", description = "根据ID查询库区详情")
    @GetMapping("/{id}")
    public Result<WarehouseVO> getById(@Parameter(description = "库区ID") @PathVariable Long id) {
        WarehouseVO vo = warehouseService.getById(id);
        return Result.success(vo);
    }

    /**
     * 根据编码查询库区
     */
    @Operation(summary = "根据编码查询库区", description = "根据库区编码查询详情")
    @GetMapping("/code/{code}")
    public Result<WarehouseVO> getByCode(@Parameter(description = "库区编码") @PathVariable String code) {
        WarehouseVO vo = warehouseService.getByCode(code);
        return Result.success(vo);
    }

    /**
     * 查询所有库区
     */
    @Operation(summary = "查询所有库区", description = "获取所有库区列表")
    @GetMapping("/list")
    public Result<List<WarehouseVO>> listAll() {
        List<WarehouseVO> list = warehouseService.listAll();
        return Result.success(list);
    }

    /**
     * 分页查询库区
     */
    @Operation(summary = "分页查询库区", description = "分页获取库区列表")
    @GetMapping("/page")
    public Result<PageResult<WarehouseVO>> page(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "库区名称") @RequestParam(required = false) String warehouseName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<WarehouseVO> page = new Page<>(current, size);
        PageResult<WarehouseVO> result = warehouseService.page(page, warehouseName, status);
        return Result.success(result);
    }
}