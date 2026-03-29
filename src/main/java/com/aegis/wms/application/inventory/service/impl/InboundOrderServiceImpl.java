package com.aegis.wms.application.inventory.service.impl;

import com.aegis.wms.application.inventory.dto.InboundOrderDTO;
import com.aegis.wms.application.inventory.service.InboundOrderService;
import com.aegis.wms.application.inventory.service.StockService;
import com.aegis.wms.application.inventory.vo.InboundOrderVO;
import com.aegis.wms.common.lock.DistributedLock;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.util.OrderNoGenerator;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.inventory.entity.GrainVariety;
import com.aegis.wms.domain.inventory.entity.InboundOrder;
import com.aegis.wms.domain.inventory.entity.StockMovement;
import com.aegis.wms.domain.masterdata.entity.Position;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.inventory.repository.GrainVarietyRepository;
import com.aegis.wms.domain.inventory.repository.InboundOrderRepository;
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
 * 入库单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundOrderServiceImpl implements InboundOrderService {

    private final InboundOrderRepository inboundOrderRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockService stockService;
    private final DistributedLock distributedLock;
    private final WarehouseRepository warehouseRepository;
    private final BinRepository binRepository;
    private final PositionRepository positionRepository;
    private final GrainVarietyRepository grainVarietyRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(InboundOrderDTO dto) {
        // 参数校验
        validateDTO(dto);

        // 生成幂等键(如果前端未传)
        String idempotentKey = dto.getIdempotentKey();
        if (idempotentKey == null || idempotentKey.isEmpty()) {
            idempotentKey = UUID.randomUUID().toString().replace("-", "");
        }

        // 幂等性前置检查：如果幂等键已存在，返回之前的入库单ID
        if (dto.getIdempotentKey() != null && !dto.getIdempotentKey().isEmpty()) {
            LambdaQueryWrapper<StockMovement> idempotentWrapper = new LambdaQueryWrapper<>();
            idempotentWrapper.eq(StockMovement::getIdempotentKey, idempotentKey);
            StockMovement existingMovement = stockMovementRepository.selectOne(idempotentWrapper);
            if (existingMovement != null) {
                log.info("幂等键已存在，返回已有入库单: {}", idempotentKey);
                return existingMovement.getOrderId();
            }
        }

        // 获取分布式锁
        boolean locked = distributedLock.tryLock(dto.getWarehouseId(), dto.getBinId(), dto.getPositionId());
        if (!locked) {
            throw new RuntimeException("当前货位业务繁忙，请稍后重试");
        }

        try {
            // 创建入库单
            InboundOrder order = new InboundOrder();
            order.setOrderNo(OrderNoGenerator.generateInboundNo());
            order.setWarehouseId(dto.getWarehouseId());
            order.setBinId(dto.getBinId());
            order.setPositionId(dto.getPositionId());
            order.setGrainVarietyId(dto.getGrainVarietyId());
            order.setHarvestYear(dto.getHarvestYear());
            order.setGrade(dto.getGrade());
            order.setQuantity(dto.getQuantity());
            order.setStatus(2); // 直接完成
            order.setRemark(dto.getRemark());
            order.setOperatorId(1L); // TODO: 从上下文获取
            order.setOperatorName("系统"); // TODO: 从上下文获取

            inboundOrderRepository.insert(order);

            // 执行入库操作
            stockService.inbound(
                    dto.getPositionId(),
                    dto.getGrainVarietyId(),
                    dto.getHarvestYear(),
                    dto.getGrade(),
                    dto.getQuantity(),
                    "INBOUND",
                    order.getId(),
                    order.getOperatorId(),
                    order.getOperatorName(),
                    idempotentKey
            );

            log.info("入库单创建成功，单号: {}, 数量: {}", order.getOrderNo(), dto.getQuantity());

            return order.getId();

        } finally {
            // 释放分布式锁
            distributedLock.unlock(dto.getWarehouseId(), dto.getBinId(), dto.getPositionId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(Long id) {
        InboundOrder order = inboundOrderRepository.selectById(id);
        if (order == null) {
            throw new RuntimeException("入库单不存在: " + id);
        }

        if (order.getStatus() != 1) {
            throw new RuntimeException("只有待审核状态的入库单才能取消");
        }

        order.setStatus(0);
        return inboundOrderRepository.updateById(order) > 0;
    }

    @Override
    public InboundOrderVO getById(Long id) {
        InboundOrder order = inboundOrderRepository.selectById(id);
        return convertToVO(order);
    }

    @Override
    public PageResult<InboundOrderVO> page(Page<InboundOrderVO> page, Long warehouseId, Long binId, Integer status) {
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId);
        wrapper.eq(binId != null, InboundOrder::getBinId, binId);
        wrapper.eq(status != null, InboundOrder::getStatus, status);
        wrapper.orderByDesc(InboundOrder::getCreateTime);

        Page<InboundOrder> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = inboundOrderRepository.selectPage(entityPage, wrapper);

        List<InboundOrderVO> voList = convertToVOList(entityPage.getRecords());
        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    @Override
    public List<InboundOrderVO> listByPositionId(Long positionId) {
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InboundOrder::getPositionId, positionId);
        wrapper.orderByDesc(InboundOrder::getCreateTime);
        List<InboundOrder> list = inboundOrderRepository.selectList(wrapper);
        return convertToVOList(list);
    }

    private void validateDTO(InboundOrderDTO dto) {
        if (dto.getWarehouseId() == null) {
            throw new RuntimeException("库区ID不能为空");
        }
        if (dto.getBinId() == null) {
            throw new RuntimeException("仓房ID不能为空");
        }
        if (dto.getPositionId() == null) {
            throw new RuntimeException("货位ID不能为空");
        }
        if (dto.getGrainVarietyId() == null) {
            throw new RuntimeException("粮食品种不能为空");
        }
        if (dto.getQuantity() == null || dto.getQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("入库数量必须大于0");
        }

        // 校验货位是否存在
        Position position = positionRepository.selectById(dto.getPositionId());
        if (position == null) {
            throw new RuntimeException("货位不存在");
        }
    }

    private List<InboundOrderVO> convertToVOList(List<InboundOrder> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }

        // 批量查询关联信息
        List<Long> warehouseIds = orders.stream().map(InboundOrder::getWarehouseId).distinct().collect(Collectors.toList());
        List<Long> binIds = orders.stream().map(InboundOrder::getBinId).distinct().collect(Collectors.toList());
        List<Long> positionIds = orders.stream().map(InboundOrder::getPositionId).distinct().collect(Collectors.toList());
        List<Long> varietyIds = orders.stream().map(InboundOrder::getGrainVarietyId).distinct().collect(Collectors.toList());

        Map<Long, String> warehouseNameMap = warehouseRepository.selectBatchIds(warehouseIds)
                .stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));
        Map<Long, Bin> binMap = binRepository.selectBatchIds(binIds)
                .stream().collect(Collectors.toMap(Bin::getId, b -> b));
        Map<Long, Position> positionMap = positionRepository.selectBatchIds(positionIds)
                .stream().collect(Collectors.toMap(Position::getId, p -> p));
        Map<Long, String> varietyNameMap = grainVarietyRepository.selectBatchIds(varietyIds)
                .stream().collect(Collectors.toMap(GrainVariety::getId, GrainVariety::getVarietyName));

        return orders.stream()
                .map(order -> {
                    Bin bin = binMap.get(order.getBinId());
                    Position position = positionMap.get(order.getPositionId());

                    InboundOrderVO vo = new InboundOrderVO();
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
                    vo.setGrainVarietyName(varietyNameMap.get(order.getGrainVarietyId()));
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

    private InboundOrderVO convertToVO(InboundOrder order) {
        if (order == null) {
            return null;
        }

        Warehouse warehouse = warehouseRepository.selectById(order.getWarehouseId());
        Bin bin = binRepository.selectById(order.getBinId());
        Position position = positionRepository.selectById(order.getPositionId());
        GrainVariety variety = grainVarietyRepository.selectById(order.getGrainVarietyId());

        InboundOrderVO vo = new InboundOrderVO();
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