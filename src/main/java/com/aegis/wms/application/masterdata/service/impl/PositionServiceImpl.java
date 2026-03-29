package com.aegis.wms.application.masterdata.service.impl;

import com.aegis.wms.application.masterdata.dto.PositionDTO;
import com.aegis.wms.application.masterdata.vo.PositionVO;
import com.aegis.wms.application.masterdata.service.PositionService;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.masterdata.entity.Position;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.masterdata.repository.PositionRepository;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 货位应用服务实现
 */
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final BinRepository binRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PositionDTO dto) {
        // 校验仓房存在
        Bin bin = binRepository.selectById(dto.getBinId());
        if (bin == null) {
            throw new RuntimeException("仓房不存在: " + dto.getBinId());
        }

        // 校验编码唯一性(同一仓房下)
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getBinId, dto.getBinId());
        wrapper.eq(Position::getPositionCode, dto.getPositionCode());
        if (positionRepository.selectCount(wrapper) > 0) {
            throw new RuntimeException("该仓房下货位编码已存在: " + dto.getPositionCode());
        }

        Position position = new Position();
        position.setBinId(dto.getBinId());
        position.setWarehouseId(dto.getWarehouseId() != null ? dto.getWarehouseId() : bin.getWarehouseId());
        position.setPositionCode(dto.getPositionCode());
        position.setPositionName(dto.getPositionName());
        position.setCapacity(dto.getCapacity());
        position.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        positionRepository.insert(position);
        return position.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PositionDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("货位ID不能为空");
        }

        Position position = positionRepository.selectById(dto.getId());
        if (position == null) {
            throw new RuntimeException("货位不存在: " + dto.getId());
        }

        // 更新字段
        if (StringUtils.hasText(dto.getPositionName())) {
            position.setPositionName(dto.getPositionName());
        }
        if (dto.getCapacity() != null) {
            position.setCapacity(dto.getCapacity());
        }
        if (dto.getStatus() != null) {
            position.setStatus(dto.getStatus());
        }

        return positionRepository.updateById(position) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        return positionRepository.deleteById(id) > 0;
    }

    @Override
    public PositionVO getById(Long id) {
        Position position = positionRepository.selectById(id);
        return convertToVO(position);
    }

    @Override
    public List<PositionVO> listByBinId(Long binId) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getBinId, binId);
        wrapper.orderByAsc(Position::getPositionCode);
        List<Position> list = positionRepository.selectList(wrapper);
        return convertToVOList(list);
    }

    @Override
    public PageResult<PositionVO> page(Page<PositionVO> page, Long binId, String positionName, Integer status) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(binId != null, Position::getBinId, binId);
        wrapper.like(StringUtils.hasText(positionName), Position::getPositionName, positionName);
        wrapper.eq(status != null, Position::getStatus, status);
        wrapper.orderByDesc(Position::getCreateTime);

        Page<Position> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = positionRepository.selectPage(entityPage, wrapper);

        List<PositionVO> voList = convertToVOList(entityPage.getRecords());
        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    @Override
    public List<PositionVO> listCascade(Long warehouseId, Long binId) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, Position::getWarehouseId, warehouseId);
        wrapper.eq(binId != null, Position::getBinId, binId);
        wrapper.eq(Position::getStatus, 1); // 只查询正常状态的货位
        wrapper.orderByAsc(Position::getPositionCode);

        List<Position> list = positionRepository.selectList(wrapper);
        return convertToVOList(list);
    }

    /**
     * 批量转换VO(带仓房和库区信息)
     */
    private List<PositionVO> convertToVOList(List<Position> positions) {
        if (positions.isEmpty()) {
            return List.of();
        }

        // 获取仓房信息映射
        List<Long> binIds = positions.stream()
                .map(Position::getBinId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Bin> binMap = binRepository.selectBatchIds(binIds)
                .stream()
                .collect(Collectors.toMap(Bin::getId, b -> b));

        // 获取库区信息映射
        List<Long> warehouseIds = positions.stream()
                .map(Position::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> warehouseNameMap = warehouseRepository.selectBatchIds(warehouseIds)
                .stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));

        return positions.stream()
                .map(p -> {
                    Bin bin = binMap.get(p.getBinId());
                    return convertToVO(p,
                            bin != null ? bin.getBinCode() : null,
                            bin != null ? bin.getBinName() : null,
                            warehouseNameMap.get(p.getWarehouseId()));
                })
                .collect(Collectors.toList());
    }

    /**
     * 实体转VO
     */
    private PositionVO convertToVO(Position position) {
        if (position == null) {
            return null;
        }
        Bin bin = binRepository.selectById(position.getBinId());
        Warehouse warehouse = warehouseRepository.selectById(position.getWarehouseId());
        return convertToVO(position,
                bin != null ? bin.getBinCode() : null,
                bin != null ? bin.getBinName() : null,
                warehouse != null ? warehouse.getWarehouseName() : null);
    }

    private PositionVO convertToVO(Position position, String binCode, String binName, String warehouseName) {
        PositionVO vo = new PositionVO();
        vo.setId(position.getId());
        vo.setBinId(position.getBinId());
        vo.setBinCode(binCode);
        vo.setBinName(binName);
        vo.setWarehouseId(position.getWarehouseId());
        vo.setWarehouseName(warehouseName);
        vo.setPositionCode(position.getPositionCode());
        vo.setPositionName(position.getPositionName());
        vo.setCapacity(position.getCapacity());
        vo.setStatus(position.getStatus());
        vo.setCreateTime(position.getCreateTime());
        vo.setUpdateTime(position.getUpdateTime());
        return vo;
    }
}