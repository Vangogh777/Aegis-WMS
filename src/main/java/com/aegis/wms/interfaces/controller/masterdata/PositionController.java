package com.aegis.wms.interfaces.controller.masterdata;

import com.aegis.wms.application.masterdata.dto.PositionDTO;
import com.aegis.wms.application.masterdata.vo.PositionVO;
import com.aegis.wms.application.masterdata.service.PositionService;
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
 * 货位控制器
 * 提供货位管理的REST API
 */
@Tag(name = "货位管理", description = "货位增删改查接口")
@RestController
@RequestMapping("/api/masterdata/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /**
     * 创建货位
     */
    @Operation(summary = "创建货位", description = "新增一个货位")
    @PostMapping
    public Result<Long> create(@RequestBody PositionDTO dto) {
        Long id = positionService.create(dto);
        return Result.success(id);
    }

    /**
     * 更新货位
     */
    @Operation(summary = "更新货位", description = "修改货位信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody PositionDTO dto) {
        Boolean result = positionService.update(dto);
        return Result.success(result);
    }

    /**
     * 删除货位
     */
    @Operation(summary = "删除货位", description = "逻辑删除货位")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@Parameter(description = "货位ID") @PathVariable Long id) {
        Boolean result = positionService.delete(id);
        return Result.success(result);
    }

    /**
     * 根据ID查询货位
     */
    @Operation(summary = "查询货位", description = "根据ID查询货位详情")
    @GetMapping("/{id}")
    public Result<PositionVO> getById(@Parameter(description = "货位ID") @PathVariable Long id) {
        PositionVO vo = positionService.getById(id);
        return Result.success(vo);
    }

    /**
     * 根据仓房ID查询货位列表
     */
    @Operation(summary = "查询仓房下的货位", description = "根据仓房ID获取货位列表")
    @GetMapping("/bin/{binId}")
    public Result<List<PositionVO>> listByBinId(@Parameter(description = "仓房ID") @PathVariable Long binId) {
        List<PositionVO> list = positionService.listByBinId(binId);
        return Result.success(list);
    }

    /**
     * 分页查询货位
     */
    @Operation(summary = "分页查询货位", description = "分页获取货位列表")
    @GetMapping("/page")
    public Result<PageResult<PositionVO>> page(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "仓房ID") @RequestParam(required = false) Long binId,
            @Parameter(description = "货位名称") @RequestParam(required = false) String positionName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<PositionVO> page = new Page<>(current, size);
        PageResult<PositionVO> result = positionService.page(page, binId, positionName, status);
        return Result.success(result);
    }

    /**
     * 级联选择查询(库区 -> 仓房 -> 货位)
     * 用于出入库单存储位置选择
     */
    @Operation(summary = "级联选择查询", description = "查询库区/仓房下的货位，用于出入库单存储位置级联选择")
    @GetMapping("/cascade")
    public Result<List<PositionVO>> listCascade(
            @Parameter(description = "库区ID") @RequestParam Long warehouseId,
            @Parameter(description = "仓房ID(可选)") @RequestParam(required = false) Long binId) {
        List<PositionVO> list = positionService.listCascade(warehouseId, binId);
        return Result.success(list);
    }
}