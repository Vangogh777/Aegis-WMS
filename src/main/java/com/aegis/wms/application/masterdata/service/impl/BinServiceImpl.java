package com.aegis.wms.application.masterdata.service.impl;

import com.aegis.wms.application.masterdata.dto.BinDTO;
import com.aegis.wms.application.masterdata.vo.BinVO;
import com.aegis.wms.application.masterdata.service.BinService;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
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
 * 仓房应用服务实现
 */
@Service
@RequiredArgsConstructor
public class BinServiceImpl implements BinService {

    private final BinRepository binRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BinDTO dto) {
        // 校验库区存在
        Warehouse warehouse = warehouseRepository.selectById(dto.getWarehouseId());
        if (warehouse == null) {
            throw new RuntimeException("库区不存在: " + dto.getWarehouseId());
        }

        // 校验编码唯一性(同一库区下)
        LambdaQueryWrapper<Bin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bin::getWarehouseId, dto.getWarehouseId());
        wrapper.eq(Bin::getBinCode, dto.getBinCode());
        if (binRepository.selectCount(wrapper) > 0) {
            throw new RuntimeException("该库区下仓房编码已存在: " + dto.getBinCode());
        }

        Bin bin = new Bin();
        bin.setWarehouseId(dto.getWarehouseId());
        bin.setBinCode(dto.getBinCode());
        bin.setBinName(dto.getBinName());
        bin.setBinType(dto.getBinType());
        bin.setCapacity(dto.getCapacity());
        bin.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        binRepository.insert(bin);
        return bin.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(BinDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("仓房ID不能为空");
        }

        Bin bin = binRepository.selectById(dto.getId());
        if (bin == null) {
            throw new RuntimeException("仓房不存在: " + dto.getId());
        }

        // 更新字段
        if (StringUtils.hasText(dto.getBinName())) {
            bin.setBinName(dto.getBinName());
        }
        if (StringUtils.hasText(dto.getBinType())) {
            bin.setBinType(dto.getBinType());
        }
        if (dto.getCapacity() != null) {
            bin.setCapacity(dto.getCapacity());
        }
        if (dto.getStatus() != null) {
            bin.setStatus(dto.getStatus());
        }

        return binRepository.updateById(bin) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        return binRepository.deleteById(id) > 0;
    }

    @Override
    public BinVO getById(Long id) {
        Bin bin = binRepository.selectById(id);
        return convertToVO(bin);
    }

    @Override
    public List<BinVO> listByWarehouseId(Long warehouseId) {
        LambdaQueryWrapper<Bin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bin::getWarehouseId, warehouseId);
        wrapper.orderByAsc(Bin::getBinCode);
        List<Bin> list = binRepository.selectList(wrapper);
        return convertToVOList(list);
    }

    @Override
    public List<BinVO> listAll() {
        LambdaQueryWrapper<Bin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bin::getStatus, 1);
        wrapper.orderByAsc(Bin::getBinCode);
        List<Bin> list = binRepository.selectList(wrapper);
        return convertToVOList(list);
    }

    @Override
    public PageResult<BinVO> page(Page<BinVO> page, Long warehouseId, String binName, Integer status) {
        LambdaQueryWrapper<Bin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, Bin::getWarehouseId, warehouseId);
        wrapper.like(StringUtils.hasText(binName), Bin::getBinName, binName);
        wrapper.eq(status != null, Bin::getStatus, status);
        wrapper.orderByDesc(Bin::getCreateTime);

        Page<Bin> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = binRepository.selectPage(entityPage, wrapper);

        List<BinVO> voList = convertToVOList(entityPage.getRecords());
        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    /**
     * 批量转换VO(带库区名称)
     */
    private List<BinVO> convertToVOList(List<Bin> bins) {
        if (bins.isEmpty()) {
            return List.of();
        }

        // 获取库区名称映射
        List<Long> warehouseIds = bins.stream()
                .map(Bin::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> warehouseNameMap = warehouseRepository.selectBatchIds(warehouseIds)
                .stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));

        return bins.stream()
                .map(bin -> convertToVO(bin, warehouseNameMap.get(bin.getWarehouseId())))
                .collect(Collectors.toList());
    }

    /**
     * 实体转VO
     */
    private BinVO convertToVO(Bin bin) {
        if (bin == null) {
            return null;
        }
        Warehouse warehouse = warehouseRepository.selectById(bin.getWarehouseId());
        return convertToVO(bin, warehouse != null ? warehouse.getWarehouseName() : null);
    }

    private BinVO convertToVO(Bin bin, String warehouseName) {
        BinVO vo = new BinVO();
        vo.setId(bin.getId());
        vo.setWarehouseId(bin.getWarehouseId());
        vo.setWarehouseName(warehouseName);
        vo.setBinCode(bin.getBinCode());
        vo.setBinName(bin.getBinName());
        vo.setBinType(bin.getBinType());
        vo.setCapacity(bin.getCapacity());
        vo.setStatus(bin.getStatus());
        vo.setCreateTime(bin.getCreateTime());
        vo.setUpdateTime(bin.getUpdateTime());
        return vo;
    }
}