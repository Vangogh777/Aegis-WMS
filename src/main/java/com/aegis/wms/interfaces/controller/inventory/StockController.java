package com.aegis.wms.interfaces.controller.inventory;

import com.aegis.wms.application.inventory.service.StockService;
import com.aegis.wms.application.inventory.vo.StockVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存控制器
 */
@Tag(name = "库存管理", description = "库存查询接口")
@RestController
@RequestMapping("/api/inventory/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "查询货位库存")
    @GetMapping("/position/{positionId}")
    public Result<StockVO> getByPositionId(@PathVariable Long positionId) {
        return Result.success(stockService.getByPositionId(positionId));
    }

    @Operation(summary = "查询仓房下所有货位库存")
    @GetMapping("/bin/{binId}")
    public Result<List<StockVO>> listByBinId(@PathVariable Long binId) {
        return Result.success(stockService.listByBinId(binId));
    }

    @Operation(summary = "查询库区下所有货位库存")
    @GetMapping("/warehouse/{warehouseId}")
    public Result<List<StockVO>> listByWarehouseId(@PathVariable Long warehouseId) {
        return Result.success(stockService.listByWarehouseId(warehouseId));
    }

    @Operation(summary = "分页查询库存")
    @GetMapping("/page")
    public Result<PageResult<StockVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId) {
        Page<StockVO> page = new Page<>(current, size);
        return Result.success(stockService.page(page, warehouseId, binId));
    }

    @Operation(summary = "计算库区库存汇总")
    @GetMapping("/sum/warehouse/{warehouseId}")
    public Result<BigDecimal> sumByWarehouseId(@PathVariable Long warehouseId) {
        return Result.success(stockService.sumByWarehouseId(warehouseId));
    }

    @Operation(summary = "计算仓房库存汇总")
    @GetMapping("/sum/bin/{binId}")
    public Result<BigDecimal> sumByBinId(@PathVariable Long binId) {
        return Result.success(stockService.sumByBinId(binId));
    }
}