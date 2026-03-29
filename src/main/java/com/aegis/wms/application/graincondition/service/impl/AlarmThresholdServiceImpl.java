package com.aegis.wms.application.graincondition.service.impl;

import com.aegis.wms.application.graincondition.dto.AlarmThresholdDTO;
import com.aegis.wms.application.graincondition.service.AlarmThresholdService;
import com.aegis.wms.application.graincondition.vo.AlarmThresholdVO;
import com.aegis.wms.domain.graincondition.entity.AlarmThreshold;
import com.aegis.wms.domain.graincondition.repository.AlarmThresholdRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 报警阈值服务实现
 */
@Service
@RequiredArgsConstructor
public class AlarmThresholdServiceImpl implements AlarmThresholdService {

    private final AlarmThresholdRepository alarmThresholdRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AlarmThresholdDTO dto) {
        // 校验编码唯一性
        LambdaQueryWrapper<AlarmThreshold> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmThreshold::getThresholdCode, dto.getThresholdCode());
        if (alarmThresholdRepository.selectCount(wrapper) > 0) {
            throw new RuntimeException("阈值编码已存在: " + dto.getThresholdCode());
        }

        AlarmThreshold threshold = new AlarmThreshold();
        threshold.setThresholdCode(dto.getThresholdCode());
        threshold.setThresholdName(dto.getThresholdName());
        threshold.setMetricType(dto.getMetricType());
        threshold.setOperator(dto.getOperator());
        threshold.setThresholdValue(dto.getThresholdValue());
        threshold.setAlarmLevel(dto.getAlarmLevel() != null ? dto.getAlarmLevel() : 1);
        threshold.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        threshold.setRemark(dto.getRemark());

        alarmThresholdRepository.insert(threshold);
        return threshold.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(AlarmThresholdDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("阈值ID不能为空");
        }

        AlarmThreshold threshold = alarmThresholdRepository.selectById(dto.getId());
        if (threshold == null) {
            throw new RuntimeException("阈值不存在: " + dto.getId());
        }

        if (dto.getThresholdName() != null) threshold.setThresholdName(dto.getThresholdName());
        if (dto.getMetricType() != null) threshold.setMetricType(dto.getMetricType());
        if (dto.getOperator() != null) threshold.setOperator(dto.getOperator());
        if (dto.getThresholdValue() != null) threshold.setThresholdValue(dto.getThresholdValue());
        if (dto.getAlarmLevel() != null) threshold.setAlarmLevel(dto.getAlarmLevel());
        if (dto.getStatus() != null) threshold.setStatus(dto.getStatus());
        if (dto.getRemark() != null) threshold.setRemark(dto.getRemark());

        return alarmThresholdRepository.updateById(threshold) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        return alarmThresholdRepository.deleteById(id) > 0;
    }

    @Override
    public AlarmThresholdVO getById(Long id) {
        AlarmThreshold threshold = alarmThresholdRepository.selectById(id);
        return convertToVO(threshold);
    }

    @Override
    public List<AlarmThresholdVO> listAllEnabled() {
        LambdaQueryWrapper<AlarmThreshold> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmThreshold::getStatus, 1);
        List<AlarmThreshold> list = alarmThresholdRepository.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private AlarmThresholdVO convertToVO(AlarmThreshold threshold) {
        if (threshold == null) {
            return null;
        }
        AlarmThresholdVO vo = new AlarmThresholdVO();
        vo.setId(threshold.getId());
        vo.setThresholdCode(threshold.getThresholdCode());
        vo.setThresholdName(threshold.getThresholdName());
        vo.setMetricType(threshold.getMetricType());
        vo.setMetricTypeName(getMetricTypeName(threshold.getMetricType()));
        vo.setOperator(threshold.getOperator());
        vo.setThresholdValue(threshold.getThresholdValue());
        vo.setAlarmLevel(threshold.getAlarmLevel());
        vo.setAlarmLevelName(getAlarmLevelName(threshold.getAlarmLevel()));
        vo.setStatus(threshold.getStatus());
        vo.setRemark(threshold.getRemark());
        vo.setCreateTime(threshold.getCreateTime());
        vo.setUpdateTime(threshold.getUpdateTime());
        return vo;
    }

    private String getMetricTypeName(String metricType) {
        if (metricType == null) return null;
        return switch (metricType) {
            case "max_grain_temp" -> "最高粮温";
            case "min_grain_temp" -> "最低粮温";
            case "avg_grain_temp" -> "平均粮温";
            case "co2_concentration" -> "CO2浓度";
            case "o2_concentration" -> "O2浓度";
            case "moisture_content" -> "粮食水分";
            default -> metricType;
        };
    }

    private String getAlarmLevelName(Integer alarmLevel) {
        if (alarmLevel == null) return null;
        return switch (alarmLevel) {
            case 1 -> "一般";
            case 2 -> "严重";
            case 3 -> "紧急";
            default -> "未知";
        };
    }
}