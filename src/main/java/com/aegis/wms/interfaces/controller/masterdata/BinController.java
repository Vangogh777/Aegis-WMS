package com.aegis.wms.interfaces.controller.masterdata;

import com.aegis.wms.application.masterdata.dto.BinDTO;
import com.aegis.wms.application.masterdata.vo.BinVO;
import com.aegis.wms.application.masterdata.service.BinService;
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
 * 仓房控制器
 * 提供仓房管理的REST API
 */
@Tag(name = "仓房管理", description = "仓房增删改查接口")
@RestController
@RequestMapping("/api/masterdata/bin")
@RequiredArgsConstructor
public class BinController {

    private final BinService binService;

    /**
     * 创建仓房
     */
    @Operation(summary = "创建仓房", description = "新增一个仓房")
    @PostMapping
    public Result<Long> create(@RequestBody BinDTO dto) {
        Long id = binService.create(dto);
        return Result.success(id);
    }

    /**
     * 更新仓房
     */
    @Operation(summary = "更新仓房", description = "修改仓房信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody BinDTO dto) {
        Boolean result = binService.update(dto);
        return Result.success(result);
    }

    /**
     * 删除仓房
     */
    @Operation(summary = "删除仓房", description = "逻辑删除仓房")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@Parameter(description = "仓房ID") @PathVariable Long id) {
        Boolean result = binService.delete(id);
        return Result.success(result);
    }

    /**
     * 根据ID查询仓房
     */
    @Operation(summary = "查询仓房", description = "根据ID查询仓房详情")
    @GetMapping("/{id}")
    public Result<BinVO> getById(@Parameter(description = "仓房ID") @PathVariable Long id) {
        BinVO vo = binService.getById(id);
        return Result.success(vo);
    }

    /**
     * 根据库区ID查询仓房列表
     */
    @Operation(summary = "查询库区下的仓房", description = "根据库区ID获取仓房列表")
    @GetMapping("/warehouse/{warehouseId}")
    public Result<List<BinVO>> listByWarehouseId(
            @Parameter(description = "库区ID") @PathVariable Long warehouseId) {
        List<BinVO> list = binService.listByWarehouseId(warehouseId);
        return Result.success(list);
    }

    /**
     * 查询所有仓房列表
     */
    @Operation(summary = "查询所有仓房", description = "获取所有仓房列表")
    @GetMapping("/list")
    public Result<List<BinVO>> listAll() {
        List<BinVO> list = binService.listAll();
        return Result.success(list);
    }

    /**
     * 分页查询仓房
     */
    @Operation(summary = "分页查询仓房", description = "分页获取仓房列表")
    @GetMapping("/page")
    public Result<PageResult<BinVO>> page(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "库区ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "仓房名称") @RequestParam(required = false) String binName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<BinVO> page = new Page<>(current, size);
        PageResult<BinVO> result = binService.page(page, warehouseId, binName, status);
        return Result.success(result);
    }
}