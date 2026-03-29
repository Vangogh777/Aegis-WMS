package com.aegis.wms.application.inventory.service.impl;

import com.aegis.wms.application.inventory.dto.GrainVarietyDTO;
import com.aegis.wms.application.inventory.service.GrainVarietyService;
import com.aegis.wms.application.inventory.vo.GrainVarietyVO;
import com.aegis.wms.domain.inventory.entity.GrainVariety;
import com.aegis.wms.domain.inventory.repository.GrainVarietyRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 粮食品种服务实现
 */
@Service
@RequiredArgsConstructor
public class GrainVarietyServiceImpl implements GrainVarietyService {

    private final GrainVarietyRepository grainVarietyRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(GrainVarietyDTO dto) {
        // 校验编码唯一性
        LambdaQueryWrapper<GrainVariety> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrainVariety::getVarietyCode, dto.getVarietyCode());
        if (grainVarietyRepository.selectCount(wrapper) > 0) {
            throw new RuntimeException("粮食品种编码已存在: " + dto.getVarietyCode());
        }

        GrainVariety variety = new GrainVariety();
        variety.setVarietyCode(dto.getVarietyCode());
        variety.setVarietyName(dto.getVarietyName());
        variety.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        variety.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        grainVarietyRepository.insert(variety);
        return variety.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(GrainVarietyDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("粮食品种ID不能为空");
        }

        GrainVariety variety = grainVarietyRepository.selectById(dto.getId());
        if (variety == null) {
            throw new RuntimeException("粮食品种不存在: " + dto.getId());
        }

        if (dto.getVarietyName() != null) {
            variety.setVarietyName(dto.getVarietyName());
        }
        if (dto.getSortOrder() != null) {
            variety.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            variety.setStatus(dto.getStatus());
        }

        return grainVarietyRepository.updateById(variety) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        return grainVarietyRepository.deleteById(id) > 0;
    }

    @Override
    public List<GrainVarietyVO> listAll() {
        LambdaQueryWrapper<GrainVariety> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrainVariety::getStatus, 1);
        wrapper.orderByAsc(GrainVariety::getSortOrder);
        List<GrainVariety> list = grainVarietyRepository.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public GrainVarietyVO getById(Long id) {
        GrainVariety variety = grainVarietyRepository.selectById(id);
        return convertToVO(variety);
    }

    private GrainVarietyVO convertToVO(GrainVariety variety) {
        if (variety == null) {
            return null;
        }
        GrainVarietyVO vo = new GrainVarietyVO();
        vo.setId(variety.getId());
        vo.setVarietyCode(variety.getVarietyCode());
        vo.setVarietyName(variety.getVarietyName());
        vo.setSortOrder(variety.getSortOrder());
        vo.setStatus(variety.getStatus());
        vo.setCreateTime(variety.getCreateTime());
        return vo;
    }
}