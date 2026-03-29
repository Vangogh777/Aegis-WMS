package com.aegis.wms.application.inventory.service.impl;

import com.aegis.wms.application.inventory.service.StockMovementService;
import com.aegis.wms.application.inventory.vo.StockMovementVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.inventory.entity.GrainVariety;
import com.aegis.wms.domain.masterdata.entity.Position;
import com.aegis.wms.domain.inventory.entity.StockMovement;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.inventory.repository.GrainVarietyRepository;
import com.aegis.wms.domain.masterdata.repository.PositionRepository;
import com.aegis.wms.domain.inventory.repository.StockMovementRepository;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存变动流水服务实现
 */
@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final PositionRepository positionRepository;
    private final WarehouseRepository warehouseRepository;
    private final BinRepository binRepository;
    private final GrainVarietyRepository grainVarietyRepository;

    @Override
    public List<StockMovementVO> listByPositionId(Long positionId) {
        LambdaQueryWrapper<StockMovement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockMovement::getPositionId, positionId);
        wrapper.orderByDesc(StockMovement::getCreateTime);
        List<StockMovement> list = stockMovementRepository.selectList(wrapper);
        return convertToVOList(list);
    }

    @Override
    public PageResult<StockMovementVO> page(Page<StockMovementVO> page, Long warehouseId, Long binId, Long positionId, Integer movementType) {
        LambdaQueryWrapper<StockMovement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, StockMovement::getWarehouseId, warehouseId);
        wrapper.eq(binId != null, StockMovement::getBinId, binId);
        wrapper.eq(positionId != null, StockMovement::getPositionId, positionId);
        wrapper.eq(movementType != null, StockMovement::getMovementType, movementType);
        wrapper.orderByDesc(StockMovement::getCreateTime);

        Page<StockMovement> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = stockMovementRepository.selectPage(entityPage, wrapper);

        List<StockMovementVO> voList = convertToVOList(entityPage.getRecords());
        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    private List<StockMovementVO> convertToVOList(List<StockMovement> movements) {
        if (movements.isEmpty()) {
            return List.of();
        }

        List<Long> warehouseIds = movements.stream().map(StockMovement::getWarehouseId).distinct().collect(Collectors.toList());
        List<Long> binIds = movements.stream().map(StockMovement::getBinId).distinct().collect(Collectors.toList());
        List<Long> positionIds = movements.stream().map(StockMovement::getPositionId).distinct().collect(Collectors.toList());
        List<Long> varietyIds = movements.stream().map(StockMovement::getGrainVarietyId).filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, String> warehouseNameMap = warehouseRepository.selectBatchIds(warehouseIds)
                .stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));
        Map<Long, String> binCodeMap = binRepository.selectBatchIds(binIds)
                .stream().collect(Collectors.toMap(Bin::getId, Bin::getBinCode));
        Map<Long, String> positionCodeMap = positionRepository.selectBatchIds(positionIds)
                .stream().collect(Collectors.toMap(Position::getId, Position::getPositionCode));
        Map<Long, String> varietyNameMap = varietyIds.isEmpty() ? Map.of() :
                grainVarietyRepository.selectBatchIds(varietyIds)
                        .stream().collect(Collectors.toMap(GrainVariety::getId, GrainVariety::getVarietyName));

        return movements.stream()
                .map(m -> {
                    StockMovementVO vo = new StockMovementVO();
                    vo.setId(m.getId());
                    vo.setMovementNo(m.getMovementNo());
                    vo.setPositionId(m.getPositionId());
                    vo.setPositionCode(positionCodeMap.get(m.getPositionId()));
                    vo.setWarehouseId(m.getWarehouseId());
                    vo.setWarehouseName(warehouseNameMap.get(m.getWarehouseId()));
                    vo.setBinId(m.getBinId());
                    vo.setBinCode(binCodeMap.get(m.getBinId()));
                    vo.setMovementType(m.getMovementType());
                    vo.setMovementTypeName(m.getMovementType() == 1 ? "入库" : "出库");
                    vo.setGrainVarietyId(m.getGrainVarietyId());
                    vo.setGrainVarietyName(m.getGrainVarietyId() != null ? varietyNameMap.get(m.getGrainVarietyId()) : null);
                    vo.setHarvestYear(m.getHarvestYear());
                    vo.setGrade(m.getGrade());
                    vo.setQuantity(m.getQuantity());
                    vo.setQuantityBefore(m.getQuantityBefore());
                    vo.setQuantityAfter(m.getQuantityAfter());
                    vo.setOrderType(m.getOrderType());
                    vo.setOperatorId(m.getOperatorId());
                    vo.setOperatorName(m.getOperatorName());
                    vo.setCreateTime(m.getCreateTime());

                    return vo;
                })
                .collect(Collectors.toList());
    }
}