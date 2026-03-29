package com.aegis.wms.interfaces.controller.inventory;

import com.aegis.wms.application.inventory.dto.OutboundOrderDTO;
import com.aegis.wms.application.inventory.service.OutboundOrderService;
import com.aegis.wms.application.inventory.vo.OutboundOrderVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 出库单控制器
 */
@Tag(name = "出库管理", description = "出库单接口")
@RestController
@RequestMapping("/api/inventory/outbound")
@RequiredArgsConstructor
public class OutboundOrderController {

    private final OutboundOrderService outboundOrderService;

    @Operation(summary = "创建出库单", description = "创建出库单并执行出库操作，包含库存校验、分布式锁和幂等性校验")
    @PostMapping
    public Result<Long> create(@RequestBody OutboundOrderDTO dto) {
        return Result.success(outboundOrderService.create(dto));
    }

    @Operation(summary = "取消出库单")
    @PutMapping("/cancel/{id}")
    public Result<Boolean> cancel(@PathVariable Long id) {
        return Result.success(outboundOrderService.cancel(id));
    }

    @Operation(summary = "查询出库单")
    @GetMapping("/{id}")
    public Result<OutboundOrderVO> getById(@PathVariable Long id) {
        return Result.success(outboundOrderService.getById(id));
    }

    @Operation(summary = "分页查询出库单")
    @GetMapping("/page")
    public Result<PageResult<OutboundOrderVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId,
            @RequestParam(required = false) Integer status) {
        Page<OutboundOrderVO> page = new Page<>(current, size);
        return Result.success(outboundOrderService.page(page, warehouseId, binId, status));
    }

    @Operation(summary = "查询货位的出库记录")
    @GetMapping("/position/{positionId}")
    public Result<List<OutboundOrderVO>> listByPositionId(@PathVariable Long positionId) {
        return Result.success(outboundOrderService.listByPositionId(positionId));
    }
}