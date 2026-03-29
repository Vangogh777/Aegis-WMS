package com.aegis.wms.application.graincondition.service.impl;

import com.aegis.wms.application.graincondition.dto.AlarmHandleDTO;
import com.aegis.wms.application.graincondition.service.AlarmRecordService;
import com.aegis.wms.application.graincondition.vo.AlarmRecordVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.util.OrderNoGenerator;
import com.aegis.wms.domain.graincondition.entity.AlarmRecord;
import com.aegis.wms.domain.graincondition.entity.AlarmThreshold;
import com.aegis.wms.domain.graincondition.repository.AlarmRecordRepository;
import com.aegis.wms.domain.graincondition.repository.AlarmThresholdRepository;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报警记录服务实现
 */
@Service
@RequiredArgsConstructor
public class AlarmRecordServiceImpl implements AlarmRecordService {

    private final AlarmRecordRepository alarmRecordRepository;
    private final AlarmThresholdRepository alarmThresholdRepository;
    private final WarehouseRepository warehouseRepository;
    private final BinRepository binRepository;

    @Override
    public PageResult<AlarmRecordVO> page(Page<AlarmRecordVO> page, Long warehouseId, Long binId, Integer status) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, AlarmRecord::getWarehouseId, warehouseId);
        wrapper.eq(binId != null, AlarmRecord::getBinId, binId);
        wrapper.eq(status != null, AlarmRecord::getStatus, status);
        wrapper.orderByDesc(AlarmRecord::getCreateTime);

        Page<AlarmRecord> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = alarmRecordRepository.selectPage(entityPage, wrapper);

        List<AlarmRecordVO> voList = convertToVOList(entityPage.getRecords());
        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    @Override
    public AlarmRecordVO getById(Long id) {
        AlarmRecord record = alarmRecordRepository.selectById(id);
        return convertToVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handle(AlarmHandleDTO dto) {
        if (dto.getAlarmId() == null) {
            throw new RuntimeException("报警ID不能为空");
        }

        AlarmRecord record = alarmRecordRepository.selectById(dto.getAlarmId());
        if (record == null) {
            throw new RuntimeException("报警记录不存在: " + dto.getAlarmId());
        }

        record.setStatus(dto.getStatus());
        record.setHandlerId(1L); // TODO: 从上下文获取
        record.setHandlerName("系统");
        record.setHandleTime(LocalDateTime.now());
        record.setHandleRemark(dto.getHandleRemark());

        return alarmRecordRepository.updateById(record) > 0;
    }

    @Override
    public Long countUnhandled(Long warehouseId) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, AlarmRecord::getWarehouseId, warehouseId);
        wrapper.eq(AlarmRecord::getStatus, 0);
        return alarmRecordRepository.selectCount(wrapper);
    }

    @Override
    public List<AlarmRecordVO> listUnhandled(Long warehouseId) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, AlarmRecord::getWarehouseId, warehouseId);
        wrapper.eq(AlarmRecord::getStatus, 0);
        wrapper.orderByDesc(AlarmRecord::getAlarmLevel);
        wrapper.orderByDesc(AlarmRecord::getCreateTime);
        List<AlarmRecord> records = alarmRecordRepository.selectList(wrapper);
        return convertToVOList(records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAlarm(Long warehouseId, Long binId, Long recordId, Long thresholdId,
                            String metricType, BigDecimal metricValue, BigDecimal thresholdValue,
                            Integer alarmLevel, String alarmContent) {
        AlarmRecord record = new AlarmRecord();
        record.setAlarmNo(generateAlarmNo());
        record.setWarehouseId(warehouseId);
        record.setBinId(binId);
        record.setRecordId(recordId);
        record.setThresholdId(thresholdId);
        record.setMetricType(metricType);
        record.setMetricValue(metricValue);
        record.setThresholdValue(thresholdValue);
        record.setAlarmLevel(alarmLevel);
        record.setAlarmContent(alarmContent);
        record.setStatus(0); // 未处理

        alarmRecordRepository.insert(record);
        return record.getId();
    }

    private String generateAlarmNo() {
        return "AL" + System.currentTimeMillis();
    }

    private List<AlarmRecordVO> convertToVOList(List<AlarmRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }

        List<Long> warehouseIds = records.stream().map(AlarmRecord::getWarehouseId).distinct().collect(Collectors.toList());
        List<Long> binIds = records.stream().map(AlarmRecord::getBinId).distinct().collect(Collectors.toList());
        List<Long> thresholdIds = records.stream().map(AlarmRecord::getThresholdId).filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, String> warehouseNameMap = warehouseRepository.selectBatchIds(warehouseIds)
                .stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));
        Map<Long, Bin> binMap = binRepository.selectBatchIds(binIds)
                .stream().collect(Collectors.toMap(Bin::getId, b -> b));
        Map<Long, String> thresholdNameMap = thresholdIds.isEmpty() ? Map.of() :
                alarmThresholdRepository.selectBatchIds(thresholdIds)
                        .stream().collect(Collectors.toMap(AlarmThreshold::getId, AlarmThreshold::getThresholdName));

        return records.stream()
                .map(record -> {
                    Bin bin = binMap.get(record.getBinId());
                    return convertToVO(record,
                            warehouseNameMap.get(record.getWarehouseId()),
                            bin != null ? bin.getBinCode() : null,
                            bin != null ? bin.getBinName() : null,
                            thresholdNameMap.get(record.getThresholdId()));
                })
                .collect(Collectors.toList());
    }

    private AlarmRecordVO convertToVO(AlarmRecord record) {
        if (record == null) {
            return null;
        }
        Warehouse warehouse = warehouseRepository.selectById(record.getWarehouseId());
        Bin bin = binRepository.selectById(record.getBinId());
        AlarmThreshold threshold = record.getThresholdId() != null ?
                alarmThresholdRepository.selectById(record.getThresholdId()) : null;

        return convertToVO(record,
                warehouse != null ? warehouse.getWarehouseName() : null,
                bin != null ? bin.getBinCode() : null,
                bin != null ? bin.getBinName() : null,
                threshold != null ? threshold.getThresholdName() : null);
    }

    private AlarmRecordVO convertToVO(AlarmRecord record, String warehouseName,
                                       String binCode, String binName, String thresholdName) {
        AlarmRecordVO vo = new AlarmRecordVO();
        vo.setId(record.getId());
        vo.setAlarmNo(record.getAlarmNo());
        vo.setWarehouseId(record.getWarehouseId());
        vo.setWarehouseName(warehouseName);
        vo.setBinId(record.getBinId());
        vo.setBinCode(binCode);
        vo.setBinName(binName);
        vo.setRecordId(record.getRecordId());
        vo.setThresholdId(record.getThresholdId());
        vo.setThresholdName(thresholdName);
        vo.setMetricType(record.getMetricType());
        vo.setMetricTypeName(getMetricTypeName(record.getMetricType()));
        vo.setMetricValue(record.getMetricValue());
        vo.setThresholdValue(record.getThresholdValue());
        vo.setAlarmLevel(record.getAlarmLevel());
        vo.setAlarmLevelName(getAlarmLevelName(record.getAlarmLevel()));
        vo.setAlarmContent(record.getAlarmContent());
        vo.setStatus(record.getStatus());
        vo.setStatusName(getStatusName(record.getStatus()));
        vo.setHandlerId(record.getHandlerId());
        vo.setHandlerName(record.getHandlerName());
        vo.setHandleTime(record.getHandleTime());
        vo.setHandleRemark(record.getHandleRemark());
        vo.setCreateTime(record.getCreateTime());
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

    private String getStatusName(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 0 -> "未处理";
            case 1 -> "已确认";
            case 2 -> "已处理";
            default -> "未知";
        };
    }
}