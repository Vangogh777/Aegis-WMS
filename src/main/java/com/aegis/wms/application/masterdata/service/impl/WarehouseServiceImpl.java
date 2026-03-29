package com.aegis.wms.application.masterdata.service.impl;

import com.aegis.wms.application.masterdata.dto.WarehouseDTO;
import com.aegis.wms.application.masterdata.vo.WarehouseVO;
import com.aegis.wms.application.masterdata.service.WarehouseService;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 库区应用服务实现
 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WarehouseDTO dto) {
        // 校验编码唯一性
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getWarehouseCode, dto.getWarehouseCode());
        if (warehouseRepository.selectCount(wrapper) > 0) {
            throw new RuntimeException("库区编码已存在: " + dto.getWarehouseCode());
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseCode(dto.getWarehouseCode());
        warehouse.setWarehouseName(dto.getWarehouseName());
        warehouse.setAddress(dto.getAddress());
        warehouse.setTotalCapacity(dto.getTotalCapacity());
        warehouse.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        warehouseRepository.insert(warehouse);
        return warehouse.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(WarehouseDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("库区ID不能为空");
        }

        Warehouse warehouse = warehouseRepository.selectById(dto.getId());
        if (warehouse == null) {
            throw new RuntimeException("库区不存在: " + dto.getId());
        }

        // 更新字段
        if (StringUtils.hasText(dto.getWarehouseName())) {
            warehouse.setWarehouseName(dto.getWarehouseName());
        }
        if (StringUtils.hasText(dto.getAddress())) {
            warehouse.setAddress(dto.getAddress());
        }
        if (dto.getTotalCapacity() != null) {
            warehouse.setTotalCapacity(dto.getTotalCapacity());
        }
        if (dto.getStatus() != null) {
            warehouse.setStatus(dto.getStatus());
        }

        return warehouseRepository.updateById(warehouse) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        return warehouseRepository.deleteById(id) > 0;
    }

    @Override
    public WarehouseVO getById(Long id) {
        Warehouse warehouse = warehouseRepository.selectById(id);
        return convertToVO(warehouse);
    }

    @Override
    public WarehouseVO getByCode(String code) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getWarehouseCode, code);
        Warehouse warehouse = warehouseRepository.selectOne(wrapper);
        return convertToVO(warehouse);
    }

    @Override
    public List<WarehouseVO> listAll() {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Warehouse::getCreateTime);
        List<Warehouse> list = warehouseRepository.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<WarehouseVO> page(Page<WarehouseVO> page, String warehouseName, Integer status) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(warehouseName), Warehouse::getWarehouseName, warehouseName);
        wrapper.eq(status != null, Warehouse::getStatus, status);
        wrapper.orderByDesc(Warehouse::getCreateTime);

        Page<Warehouse> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = warehouseRepository.selectPage(entityPage, wrapper);

        List<WarehouseVO> voList = entityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    /**
     * 实体转VO
     */
    private WarehouseVO convertToVO(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        WarehouseVO vo = new WarehouseVO();
        vo.setId(warehouse.getId());
        vo.setWarehouseCode(warehouse.getWarehouseCode());
        vo.setWarehouseName(warehouse.getWarehouseName());
        vo.setAddress(warehouse.getAddress());
        vo.setTotalCapacity(warehouse.getTotalCapacity());
        vo.setStatus(warehouse.getStatus());
        vo.setCreateTime(warehouse.getCreateTime());
        vo.setUpdateTime(warehouse.getUpdateTime());
        return vo;
    }
}