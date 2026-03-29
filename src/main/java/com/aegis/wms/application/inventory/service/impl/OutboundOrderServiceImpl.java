package com.aegis.wms.application.inventory.service.impl;

import com.aegis.wms.application.inventory.dto.OutboundOrderDTO;
import com.aegis.wms.application.inventory.service.OutboundOrderService;
import com.aegis.wms.application.inventory.service.StockService;
import com.aegis.wms.application.inventory.vo.OutboundOrderVO;
import com.aegis.wms.common.lock.DistributedLock;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.util.OrderNoGenerator;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.inventory.entity.GrainVariety;
import com.aegis.wms.domain.inventory.entity.OutboundOrder;
import com.aegis.wms.domain.inventory.entity.StockMovement;
import com.aegis.wms.domain.masterdata.entity.Position;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.inventory.repository.GrainVarietyRepository;
import com.aegis.wms.domain.inventory.repository.OutboundOrderRepository;
import com.aegis.wms.domain.inventory.repository.StockMovementRepository;
import com.aegis.wms.domain.masterdata.repository.PositionRepository;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 出库单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundOrderServiceImpl implements OutboundOrderService {

    private final OutboundOrderRepository outboundOrderRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockService stockService;
    private final DistributedLock distributedLock;
    private final WarehouseRepository warehouseRepository;
    private final BinRepository binRepository;
    private final PositionRepository positionRepository;
    private final GrainVarietyRepository grainVarietyRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OutboundOrderDTO dto) {
        // 参数校验
        validateDTO(dto);

        // 生成幂等键
        String idempotentKey = dto.getIdempotentKey();
        if (idempotentKey == null || idempotentKey.isEmpty()) {
            idempotentKey = UUID.randomUUID().toString().replace("-", "");
        }

        // 幂等性前置检查：如果幂等键已存在，返回之前的出库单ID
        if (dto.getIdempotentKey() != null && !dto.getIdempotentKey().isEmpty()) {
            LambdaQueryWrapper<StockMovement> idempotentWrapper = new LambdaQueryWrapper<>();
            idempotentWrapper.eq(StockMovement::getIdempotentKey, idempotentKey);
            StockMovement existingMovement = stockMovementRepository.selectOne(idempotentWrapper);
            if (existingMovement != null) {
                log.info("幂等键已存在，返回已有出库单: {}", idempotentKey);
                return existingMovement.getOrderId();
            }
        }

        // 获取分布式锁
        boolean locked = distributedLock.tryLock(dto.getWarehouseId(), dto.getBinId(), dto.getPositionId());
        if (!locked) {
            throw new RuntimeException("当前货位业务繁忙，请稍后重试");
        }

        try {
            // 创建出库单
            OutboundOrder order = new OutboundOrder();
            order.setOrderNo(OrderNoGenerator.generateOutboundNo());
            order.setWarehouseId(dto.getWarehouseId());
            order.setBinId(dto.getBinId());
            order.setPositionId(dto.getPositionId());
            order.setGrainVarietyId(dto.getGrainVarietyId());
            order.setHarvestYear(dto.getHarvestYear());
            order.setGrade(dto.getGrade());
            order.setQuantity(dto.getQuantity());
            order.setStatus(2); // 直接完成
            order.setRemark(dto.getRemark());
            order.setOperatorId(1L);
            order.setOperatorName("系统");

            outboundOrderRepository.insert(order);

            // 执行出库操作（包含库存校验）
            stockService.outbound(
                    dto.getPositionId(),
                    dto.getQuantity(),
                    "OUTBOUND",
                    order.getId(),
                    order.getOperatorId(),
                    order.getOperatorName(),
                    idempotentKey
            );

            log.info("出库单创建成功，单号: {}, 数量: {}", order.getOrderNo(), dto.getQuantity());

            return order.getId();

        } finally {
            // 释放分布式锁
            distributedLock.unlock(dto.getWarehouseId(), dto.getBinId(), dto.getPositionId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(Long id) {
        OutboundOrder order = outboundOrderRepository.selectById(id);
        if (order == null) {
            throw new RuntimeException("出库单不存在: " + id);
        }

        if (order.getStatus() != 1) {
            throw new RuntimeException("只有待审核状态的出库单才能取消");
        }

        order.setStatus(0);
        return outboundOrderRepository.updateById(order) > 0;
    }

    @Override
    public OutboundOrderVO getById(Long id) {
        OutboundOrder order = outboundOrderRepository.selectById(id);
        return convertToVO(order);
    }

    @Override
    public PageResult<OutboundOrderVO> page(Page<OutboundOrderVO> page, Long warehouseId, Long binId, Integer status) {
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId);
        wrapper.eq(binId != null, OutboundOrder::getBinId, binId);
        wrapper.eq(status != null, OutboundOrder::getStatus, status);
        wrapper.orderByDesc(OutboundOrder::getCreateTime);

        Page<OutboundOrder> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = outboundOrderRepository.selectPage(entityPage, wrapper);

        List<OutboundOrderVO> voList = convertToVOList(entityPage.getRecords());
        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    @Override
    public List<OutboundOrderVO> listByPositionId(Long positionId) {
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundOrder::getPositionId, positionId);
        wrapper.orderByDesc(OutboundOrder::getCreateTime);
        List<OutboundOrder> list = outboundOrderRepository.selectList(wrapper);
        return convertToVOList(list);
    }

    private void validateDTO(OutboundOrderDTO dto) {
        if (dto.getWarehouseId() == null) {
            throw new RuntimeException("库区ID不能为空");
        }
        if (dto.getBinId() == null) {
            throw new RuntimeException("仓房ID不能为空");
        }
        if (dto.getPositionId() == null) {
            throw new RuntimeException("货位ID不能为空");
        }
        if (dto.getQuantity() == null || dto.getQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("出库数量必须大于0");
        }

        Position position = positionRepository.selectById(dto.getPositionId());
        if (position == null) {
            throw new RuntimeException("货位不存在");
        }
    }

    private List<OutboundOrderVO> convertToVOList(List<OutboundOrder> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> warehouseIds = orders.stream().map(OutboundOrder::getWarehouseId).distinct().collect(Collectors.toList());
        List<Long> binIds = orders.stream().map(OutboundOrder::getBinId).distinct().collect(Collectors.toList());
        List<Long> positionIds = orders.stream().map(OutboundOrder::getPositionId).distinct().collect(Collectors.toList());
        List<Long> varietyIds = orders.stream().map(OutboundOrder::getGrainVarietyId).filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, String> warehouseNameMap = warehouseRepository.selectBatchIds(warehouseIds)
                .stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));
        Map<Long, Bin> binMap = binRepository.selectBatchIds(binIds)
                .stream().collect(Collectors.toMap(Bin::getId, b -> b));
        Map<Long, Position> positionMap = positionRepository.selectBatchIds(positionIds)
                .stream().collect(Collectors.toMap(Position::getId, p -> p));
        Map<Long, String> varietyNameMap = varietyIds.isEmpty() ? Map.of() :
                grainVarietyRepository.selectBatchIds(varietyIds)
                        .stream().collect(Collectors.toMap(GrainVariety::getId, GrainVariety::getVarietyName));

        return orders.stream()
                .map(order -> {
                    Bin bin = binMap.get(order.getBinId());
                    Position position = positionMap.get(order.getPositionId());

                    OutboundOrderVO vo = new OutboundOrderVO();
                    vo.setId(order.getId());
                    vo.setOrderNo(order.getOrderNo());
                    vo.setWarehouseId(order.getWarehouseId());
                    vo.setWarehouseName(warehouseNameMap.get(order.getWarehouseId()));
                    vo.setBinId(order.getBinId());
                    vo.setBinCode(bin != null ? bin.getBinCode() : null);
                    vo.setBinName(bin != null ? bin.getBinName() : null);
                    vo.setPositionId(order.getPositionId());
                    vo.setPositionCode(position != null ? position.getPositionCode() : null);
                    vo.setPositionName(position != null ? position.getPositionName() : null);
                    vo.setGrainVarietyId(order.getGrainVarietyId());
                    vo.setGrainVarietyName(order.getGrainVarietyId() != null ? varietyNameMap.get(order.getGrainVarietyId()) : null);
                    vo.setHarvestYear(order.getHarvestYear());
                    vo.setGrade(order.getGrade());
                    vo.setQuantity(order.getQuantity());
                    vo.setStatus(order.getStatus());
                    vo.setStatusName(getStatusName(order.getStatus()));
                    vo.setRemark(order.getRemark());
                    vo.setOperatorId(order.getOperatorId());
                    vo.setOperatorName(order.getOperatorName());
                    vo.setCreateTime(order.getCreateTime());
                    vo.setUpdateTime(order.getUpdateTime());

                    return vo;
                })
                .collect(Collectors.toList());
    }

    private OutboundOrderVO convertToVO(OutboundOrder order) {
        if (order == null) {
            return null;
        }

        Warehouse warehouse = warehouseRepository.selectById(order.getWarehouseId());
        Bin bin = binRepository.selectById(order.getBinId());
        Position position = positionRepository.selectById(order.getPositionId());
        GrainVariety variety = order.getGrainVarietyId() != null ?
                grainVarietyRepository.selectById(order.getGrainVarietyId()) : null;

        OutboundOrderVO vo = new OutboundOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setWarehouseId(order.getWarehouseId());
        vo.setWarehouseName(warehouse != null ? warehouse.getWarehouseName() : null);
        vo.setBinId(order.getBinId());
        vo.setBinCode(bin != null ? bin.getBinCode() : null);
        vo.setBinName(bin != null ? bin.getBinName() : null);
        vo.setPositionId(order.getPositionId());
        vo.setPositionCode(position != null ? position.getPositionCode() : null);
        vo.setPositionName(position != null ? position.getPositionName() : null);
        vo.setGrainVarietyId(order.getGrainVarietyId());
        vo.setGrainVarietyName(variety != null ? variety.getVarietyName() : null);
        vo.setHarvestYear(order.getHarvestYear());
        vo.setGrade(order.getGrade());
        vo.setQuantity(order.getQuantity());
        vo.setStatus(order.getStatus());
        vo.setStatusName(getStatusName(order.getStatus()));
        vo.setRemark(order.getRemark());
        vo.setOperatorId(order.getOperatorId());
        vo.setOperatorName(order.getOperatorName());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());

        return vo;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "已取消";
            case 1 -> "待审核";
            case 2 -> "已完成";
            default -> "未知";
        };
    }
}