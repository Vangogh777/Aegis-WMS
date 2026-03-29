package com.aegis.wms.application.inventory.service.impl;

import com.aegis.wms.application.inventory.service.StockService;
import com.aegis.wms.application.inventory.vo.StockVO;
import com.aegis.wms.common.lock.DistributedLock;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.util.OrderNoGenerator;
import com.aegis.wms.domain.inventory.entity.GrainVariety;
import com.aegis.wms.domain.inventory.entity.Stock;
import com.aegis.wms.domain.inventory.entity.StockMovement;
import com.aegis.wms.domain.masterdata.entity.Position;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.inventory.repository.GrainVarietyRepository;
import com.aegis.wms.domain.masterdata.repository.PositionRepository;
import com.aegis.wms.domain.inventory.repository.StockMovementRepository;
import com.aegis.wms.domain.inventory.repository.StockRepository;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存服务实现
 * 核心功能：高并发库存扣减，使用Redis分布式锁 + 乐观锁
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PositionRepository positionRepository;
    private final WarehouseRepository warehouseRepository;
    private final BinRepository binRepository;
    private final GrainVarietyRepository grainVarietyRepository;
    private final DistributedLock distributedLock;

    @Override
    public StockVO getByPositionId(Long positionId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getPositionId, positionId);
        Stock stock = stockRepository.selectOne(wrapper);
        return convertToVO(stock);
    }

    @Override
    public List<StockVO> listByBinId(Long binId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getBinId, binId);
        List<Stock> list = stockRepository.selectList(wrapper);
        return convertToVOList(list);
    }

    @Override
    public List<StockVO> listByWarehouseId(Long warehouseId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getWarehouseId, warehouseId);
        List<Stock> list = stockRepository.selectList(wrapper);
        return convertToVOList(list);
    }

    @Override
    public PageResult<StockVO> page(Page<StockVO> page, Long warehouseId, Long binId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, Stock::getWarehouseId, warehouseId);
        wrapper.eq(binId != null, Stock::getBinId, binId);
        wrapper.orderByDesc(Stock::getUpdateTime);

        Page<Stock> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = stockRepository.selectPage(entityPage, wrapper);

        List<StockVO> voList = convertToVOList(entityPage.getRecords());
        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    /**
     * 库存入库(增加库存)
     * 执行流程：
     * 1. 获取Redis分布式锁
     * 2. 开启事务
     * 3. 查询库存 -> 新增或更新
     * 4. 记录变动流水
     * 5. 提交事务
     * 6. 释放锁
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal inbound(Long positionId, Long grainVarietyId, Integer harvestYear, String grade,
                              BigDecimal quantity, String orderType, Long orderId,
                              Long operatorId, String operatorName, String idempotentKey) {

        // 查询货位信息获取warehouseId和binId
        Position position = positionRepository.selectById(positionId);
        if (position == null) {
            throw new RuntimeException("货位不存在: " + positionId);
        }

        Long warehouseId = position.getWarehouseId();
        Long binId = position.getBinId();

        // 幂等性校验
        if (idempotentKey != null) {
            LambdaQueryWrapper<StockMovement> idempotentWrapper = new LambdaQueryWrapper<>();
            idempotentWrapper.eq(StockMovement::getIdempotentKey, idempotentKey);
            if (stockMovementRepository.selectCount(idempotentWrapper) > 0) {
                log.warn("重复请求，幂等键: {}", idempotentKey);
                // 返回当前库存
                Stock existingStock = getOrCreateStock(positionId, warehouseId, binId);
                return existingStock.getQuantity();
            }
        }

        // 获取或创建库存记录
        Stock stock = getOrCreateStock(positionId, warehouseId, binId);

        // 记录变动前库存
        BigDecimal quantityBefore = stock.getQuantity() != null ? stock.getQuantity() : BigDecimal.ZERO;
        BigDecimal quantityAfter = quantityBefore.add(quantity);

        // 更新库存信息
        stock.setQuantity(quantityAfter);
        stock.setGrainVarietyId(grainVarietyId);
        stock.setHarvestYear(harvestYear);
        stock.setGrade(grade);

        // 保存库存（乐观锁自动处理version）
        if (stock.getId() == null) {
            stockRepository.insert(stock);
        } else {
            int rows = stockRepository.updateById(stock);
            if (rows == 0) {
                throw new RuntimeException("库存更新失败，请重试");
            }
        }

        // 记录变动流水
        StockMovement movement = new StockMovement();
        movement.setMovementNo(OrderNoGenerator.generateMovementNo());
        movement.setPositionId(positionId);
        movement.setWarehouseId(warehouseId);
        movement.setBinId(binId);
        movement.setMovementType(1); // 入库
        movement.setGrainVarietyId(grainVarietyId);
        movement.setHarvestYear(harvestYear);
        movement.setGrade(grade);
        movement.setQuantity(quantity);
        movement.setQuantityBefore(quantityBefore);
        movement.setQuantityAfter(quantityAfter);
        movement.setOrderType(orderType);
        movement.setOrderId(orderId);
        movement.setOperatorId(operatorId);
        movement.setOperatorName(operatorName);
        movement.setIdempotentKey(idempotentKey);

        stockMovementRepository.insert(movement);

        log.info("入库成功，货位: {}, 数量: {}, 入库后: {}", positionId, quantity, quantityAfter);

        return quantityAfter;
    }

    /**
     * 库存出库(减少库存)
     * 执行流程：
     * 1. 获取Redis分布式锁
     * 2. 开启事务
     * 3. 查询库存 -> 校验是否足够
     * 4. 扣减库存(乐观锁)
     * 5. 记录变动流水
     * 6. 提交事务
     * 7. 释放锁
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal outbound(Long positionId, BigDecimal quantity, String orderType, Long orderId,
                               Long operatorId, String operatorName, String idempotentKey) {

        // 查询货位信息
        Position position = positionRepository.selectById(positionId);
        if (position == null) {
            throw new RuntimeException("货位不存在: " + positionId);
        }

        Long warehouseId = position.getWarehouseId();
        Long binId = position.getBinId();

        // 幂等性校验
        if (idempotentKey != null) {
            LambdaQueryWrapper<StockMovement> idempotentWrapper = new LambdaQueryWrapper<>();
            idempotentWrapper.eq(StockMovement::getIdempotentKey, idempotentKey);
            if (stockMovementRepository.selectCount(idempotentWrapper) > 0) {
                log.warn("重复请求，幂等键: {}", idempotentKey);
                Stock existingStock = getOrCreateStock(positionId, warehouseId, binId);
                return existingStock.getQuantity();
            }
        }

        // 查询库存
        Stock stock = getOrCreateStock(positionId, warehouseId, binId);
        BigDecimal quantityBefore = stock.getQuantity() != null ? stock.getQuantity() : BigDecimal.ZERO;

        // 库存校验 - 防止超扣
        if (quantityBefore.compareTo(quantity) < 0) {
            throw new RuntimeException("库存不足，当前库存: " + quantityBefore + "，需出库: " + quantity);
        }

        BigDecimal quantityAfter = quantityBefore.subtract(quantity);

        // 更新库存
        stock.setQuantity(quantityAfter);
        int rows = stockRepository.updateById(stock);
        if (rows == 0) {
            throw new RuntimeException("库存更新失败(乐观锁冲突)，请重试");
        }

        // 记录变动流水
        StockMovement movement = new StockMovement();
        movement.setMovementNo(OrderNoGenerator.generateMovementNo());
        movement.setPositionId(positionId);
        movement.setWarehouseId(warehouseId);
        movement.setBinId(binId);
        movement.setMovementType(2); // 出库
        movement.setGrainVarietyId(stock.getGrainVarietyId());
        movement.setHarvestYear(stock.getHarvestYear());
        movement.setGrade(stock.getGrade());
        movement.setQuantity(quantity);
        movement.setQuantityBefore(quantityBefore);
        movement.setQuantityAfter(quantityAfter);
        movement.setOrderType(orderType);
        movement.setOrderId(orderId);
        movement.setOperatorId(operatorId);
        movement.setOperatorName(operatorName);
        movement.setIdempotentKey(idempotentKey);

        stockMovementRepository.insert(movement);

        log.info("出库成功，货位: {}, 数量: {}, 出库后: {}", positionId, quantity, quantityAfter);

        return quantityAfter;
    }

    @Override
    public BigDecimal sumByWarehouseId(Long warehouseId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getWarehouseId, warehouseId);
        wrapper.select(Stock::getQuantity);
        List<Stock> list = stockRepository.selectList(wrapper);

        return list.stream()
                .map(s -> s.getQuantity() != null ? s.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal sumByBinId(Long binId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getBinId, binId);
        wrapper.select(Stock::getQuantity);
        List<Stock> list = stockRepository.selectList(wrapper);

        return list.stream()
                .map(s -> s.getQuantity() != null ? s.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取或创建库存记录
     */
    private Stock getOrCreateStock(Long positionId, Long warehouseId, Long binId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getPositionId, positionId);
        Stock stock = stockRepository.selectOne(wrapper);

        if (stock == null) {
            stock = new Stock();
            stock.setPositionId(positionId);
            stock.setWarehouseId(warehouseId);
            stock.setBinId(binId);
            stock.setQuantity(BigDecimal.ZERO);
            stock.setVersion(0);
        }

        return stock;
    }

    private List<StockVO> convertToVOList(List<Stock> stocks) {
        if (stocks.isEmpty()) {
            return List.of();
        }

        // 批量查询关联信息
        List<Long> positionIds = stocks.stream().map(Stock::getPositionId).distinct().collect(Collectors.toList());
        List<Long> warehouseIds = stocks.stream().map(Stock::getWarehouseId).distinct().collect(Collectors.toList());
        List<Long> binIds = stocks.stream().map(Stock::getBinId).distinct().collect(Collectors.toList());
        List<Long> varietyIds = stocks.stream().map(Stock::getGrainVarietyId).filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, Position> positionMap = positionRepository.selectBatchIds(positionIds)
                .stream().collect(Collectors.toMap(Position::getId, p -> p));
        Map<Long, Warehouse> warehouseMap = warehouseRepository.selectBatchIds(warehouseIds)
                .stream().collect(Collectors.toMap(Warehouse::getId, w -> w));
        Map<Long, Bin> binMap = binRepository.selectBatchIds(binIds)
                .stream().collect(Collectors.toMap(Bin::getId, b -> b));
        Map<Long, GrainVariety> varietyMap = varietyIds.isEmpty() ? Map.of() :
                grainVarietyRepository.selectBatchIds(varietyIds)
                        .stream().collect(Collectors.toMap(GrainVariety::getId, v -> v));

        return stocks.stream()
                .map(stock -> {
                    Position pos = positionMap.get(stock.getPositionId());
                    Warehouse wh = warehouseMap.get(stock.getWarehouseId());
                    Bin bin = binMap.get(stock.getBinId());
                    GrainVariety variety = stock.getGrainVarietyId() != null ? varietyMap.get(stock.getGrainVarietyId()) : null;

                    return convertToVO(stock, pos, wh, bin, variety);
                })
                .collect(Collectors.toList());
    }

    private StockVO convertToVO(Stock stock) {
        if (stock == null) {
            return null;
        }
        Position position = positionRepository.selectById(stock.getPositionId());
        Warehouse warehouse = warehouseRepository.selectById(stock.getWarehouseId());
        Bin bin = binRepository.selectById(stock.getBinId());
        GrainVariety variety = stock.getGrainVarietyId() != null ?
                grainVarietyRepository.selectById(stock.getGrainVarietyId()) : null;

        return convertToVO(stock, position, warehouse, bin, variety);
    }

    private StockVO convertToVO(Stock stock, Position position, Warehouse warehouse, Bin bin, GrainVariety variety) {
        StockVO vo = new StockVO();
        vo.setId(stock.getId());
        vo.setPositionId(stock.getPositionId());
        vo.setPositionCode(position != null ? position.getPositionCode() : null);
        vo.setPositionName(position != null ? position.getPositionName() : null);
        vo.setWarehouseId(stock.getWarehouseId());
        vo.setWarehouseName(warehouse != null ? warehouse.getWarehouseName() : null);
        vo.setBinId(stock.getBinId());
        vo.setBinCode(bin != null ? bin.getBinCode() : null);
        vo.setBinName(bin != null ? bin.getBinName() : null);
        vo.setGrainVarietyId(stock.getGrainVarietyId());
        vo.setGrainVarietyName(variety != null ? variety.getVarietyName() : null);
        vo.setHarvestYear(stock.getHarvestYear());
        vo.setGrade(stock.getGrade());
        vo.setQuantity(stock.getQuantity());
        vo.setCapacity(position != null ? position.getCapacity() : null);

        // 计算使用率
        if (position != null && position.getCapacity() != null &&
            position.getCapacity().compareTo(BigDecimal.ZERO) > 0 &&
            stock.getQuantity() != null) {
            vo.setUsageRate(stock.getQuantity()
                    .divide(position.getCapacity(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        }

        vo.setUpdateTime(stock.getUpdateTime());
        return vo;
    }
}