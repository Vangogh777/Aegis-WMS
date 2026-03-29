package com.aegis.wms.interfaces.controller.inventory;

import com.aegis.wms.application.inventory.dto.InboundOrderDTO;
import com.aegis.wms.application.inventory.service.InboundOrderService;
import com.aegis.wms.application.inventory.vo.InboundOrderVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入库单控制器
 */
@Tag(name = "入库管理", description = "入库单接口")
@RestController
@RequestMapping("/api/inventory/inbound")
@RequiredArgsConstructor
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    @Operation(summary = "创建入库单", description = "创建入库单并执行入库操作，包含分布式锁和幂等性校验")
    @PostMapping
    public Result<Long> create(@RequestBody InboundOrderDTO dto) {
        return Result.success(inboundOrderService.create(dto));
    }

    @Operation(summary = "取消入库单")
    @PutMapping("/cancel/{id}")
    public Result<Boolean> cancel(@PathVariable Long id) {
        return Result.success(inboundOrderService.cancel(id));
    }

    @Operation(summary = "查询入库单")
    @GetMapping("/{id}")
    public Result<InboundOrderVO> getById(@PathVariable Long id) {
        return Result.success(inboundOrderService.getById(id));
    }

    @Operation(summary = "分页查询入库单")
    @GetMapping("/page")
    public Result<PageResult<InboundOrderVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId,
            @RequestParam(required = false) Integer status) {
        Page<InboundOrderVO> page = new Page<>(current, size);
        return Result.success(inboundOrderService.page(page, warehouseId, binId, status));
    }

    @Operation(summary = "查询货位的入库记录")
    @GetMapping("/position/{positionId}")
    public Result<List<InboundOrderVO>> listByPositionId(@PathVariable Long positionId) {
        return Result.success(inboundOrderService.listByPositionId(positionId));
    }
}