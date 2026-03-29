package com.aegis.wms.interfaces.controller.inventory;

import com.aegis.wms.application.inventory.service.StockMovementService;
import com.aegis.wms.application.inventory.vo.StockMovementVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存变动流水控制器
 */
@Tag(name = "库存流水", description = "库存变动流水接口")
@RestController
@RequestMapping("/api/inventory/movement")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @Operation(summary = "查询货位的变动流水")
    @GetMapping("/position/{positionId}")
    public Result<List<StockMovementVO>> listByPositionId(@PathVariable Long positionId) {
        return Result.success(stockMovementService.listByPositionId(positionId));
    }

    @Operation(summary = "分页查询变动流水")
    @GetMapping("/page")
    public Result<PageResult<StockMovementVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) Integer movementType) {
        Page<StockMovementVO> page = new Page<>(current, size);
        return Result.success(stockMovementService.page(page, warehouseId, binId, positionId, movementType));
    }
}