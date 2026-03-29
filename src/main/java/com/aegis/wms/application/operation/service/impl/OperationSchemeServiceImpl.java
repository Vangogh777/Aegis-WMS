package com.aegis.wms.application.operation.service.impl;

import com.aegis.wms.application.operation.dto.OperationSchemeDTO;
import com.aegis.wms.application.operation.service.OperationSchemeService;
import com.aegis.wms.application.operation.vo.OperationSchemeVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.operation.entity.OperationScheme;
import com.aegis.wms.domain.operation.repository.OperationSchemeRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 作业方案服务实现
 */
@Service
@RequiredArgsConstructor
public class OperationSchemeServiceImpl implements OperationSchemeService {

    private final OperationSchemeRepository operationSchemeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OperationSchemeDTO dto) {
        // 校验编码唯一性
        LambdaQueryWrapper<OperationScheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationScheme::getSchemeCode, dto.getSchemeCode());
        if (operationSchemeRepository.selectCount(wrapper) > 0) {
            throw new RuntimeException("方案编码已存在: " + dto.getSchemeCode());
        }

        OperationScheme scheme = new OperationScheme();
        scheme.setSchemeCode(dto.getSchemeCode());
        scheme.setSchemeName(dto.getSchemeName());
        scheme.setOperationType(dto.getOperationType());
        scheme.setConfigParams(dto.getConfigParams());
        scheme.setTempThreshold(dto.getTempThreshold());
        scheme.setHumidityThreshold(dto.getHumidityThreshold());
        scheme.setDurationMax(dto.getDurationMax());
        scheme.setDescription(dto.getDescription());
        scheme.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        scheme.setCreateBy(dto.getCreateBy());
        scheme.setCreateByName(dto.getCreateByName());

        operationSchemeRepository.insert(scheme);
        return scheme.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(OperationSchemeDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("方案ID不能为空");
        }

        OperationScheme scheme = operationSchemeRepository.selectById(dto.getId());
        if (scheme == null) {
            throw new RuntimeException("方案不存在: " + dto.getId());
        }

        if (StringUtils.hasText(dto.getSchemeName())) {
            scheme.setSchemeName(dto.getSchemeName());
        }
        if (StringUtils.hasText(dto.getOperationType())) {
            scheme.setOperationType(dto.getOperationType());
        }
        if (dto.getConfigParams() != null) {
            scheme.setConfigParams(dto.getConfigParams());
        }
        if (dto.getTempThreshold() != null) {
            scheme.setTempThreshold(dto.getTempThreshold());
        }
        if (dto.getHumidityThreshold() != null) {
            scheme.setHumidityThreshold(dto.getHumidityThreshold());
        }
        if (dto.getDurationMax() != null) {
            scheme.setDurationMax(dto.getDurationMax());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            scheme.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            scheme.setStatus(dto.getStatus());
        }

        return operationSchemeRepository.updateById(scheme) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        return operationSchemeRepository.deleteById(id) > 0;
    }

    @Override
    public OperationSchemeVO getById(Long id) {
        OperationScheme scheme = operationSchemeRepository.selectById(id);
        return convertToVO(scheme);
    }

    @Override
    public OperationSchemeVO getByCode(String code) {
        LambdaQueryWrapper<OperationScheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationScheme::getSchemeCode, code);
        OperationScheme scheme = operationSchemeRepository.selectOne(wrapper);
        return convertToVO(scheme);
    }

    @Override
    public List<OperationSchemeVO> listAllEnabled() {
        LambdaQueryWrapper<OperationScheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationScheme::getStatus, 1);
        wrapper.orderByDesc(OperationScheme::getCreateTime);
        List<OperationScheme> list = operationSchemeRepository.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<OperationSchemeVO> listByType(String operationType) {
        LambdaQueryWrapper<OperationScheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationScheme::getOperationType, operationType);
        wrapper.eq(OperationScheme::getStatus, 1);
        wrapper.orderByDesc(OperationScheme::getCreateTime);
        List<OperationScheme> list = operationSchemeRepository.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<OperationSchemeVO> page(Page<OperationSchemeVO> page, String schemeName, String operationType, Integer status) {
        LambdaQueryWrapper<OperationScheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(schemeName), OperationScheme::getSchemeName, schemeName);
        wrapper.eq(StringUtils.hasText(operationType), OperationScheme::getOperationType, operationType);
        wrapper.eq(status != null, OperationScheme::getStatus, status);
        wrapper.orderByDesc(OperationScheme::getCreateTime);

        Page<OperationScheme> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = operationSchemeRepository.selectPage(entityPage, wrapper);

        List<OperationSchemeVO> voList = entityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    /**
     * 实体转VO
     */
    private OperationSchemeVO convertToVO(OperationScheme scheme) {
        if (scheme == null) {
            return null;
        }
        OperationSchemeVO vo = new OperationSchemeVO();
        vo.setId(scheme.getId());
        vo.setSchemeCode(scheme.getSchemeCode());
        vo.setSchemeName(scheme.getSchemeName());
        vo.setOperationType(scheme.getOperationType());
        vo.setOperationTypeName(getOperationTypeName(scheme.getOperationType()));
        vo.setConfigParams(scheme.getConfigParams());
        vo.setTempThreshold(scheme.getTempThreshold());
        vo.setHumidityThreshold(scheme.getHumidityThreshold());
        vo.setDurationMax(scheme.getDurationMax());
        vo.setDescription(scheme.getDescription());
        vo.setStatus(scheme.getStatus());
        vo.setStatusName(scheme.getStatus() == 1 ? "启用" : "禁用");
        vo.setCreateBy(scheme.getCreateBy());
        vo.setCreateByName(scheme.getCreateByName());
        vo.setCreateTime(scheme.getCreateTime());
        vo.setUpdateTime(scheme.getUpdateTime());
        return vo;
    }

    /**
     * 获取作业类型名称
     */
    private String getOperationTypeName(String operationType) {
        if (operationType == null) {
            return null;
        }
        switch (operationType) {
            case "ventilation":
                return "通风作业";
            case "aeration":
                return "气调作业";
            case "temperature":
                return "控温作业";
            case "fumigation":
                return "熏蒸作业";
            default:
                return operationType;
        }
    }
}