package com.aegis.wms.application.graincondition.service.impl;

import com.aegis.wms.application.graincondition.service.AlarmCheckService;
import com.aegis.wms.application.graincondition.service.AlarmRecordService;
import com.aegis.wms.domain.graincondition.entity.AlarmThreshold;
import com.aegis.wms.domain.graincondition.entity.GrainConditionRecord;
import com.aegis.wms.domain.graincondition.repository.AlarmThresholdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报警检测服务实现
 * Kafka消费者调用，异步检测报警
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmCheckServiceImpl implements AlarmCheckService {

    private final AlarmThresholdRepository alarmThresholdRepository;
    private final AlarmRecordService alarmRecordService;

    @Override
    public void checkAndCreateAlarms(GrainConditionRecord record) {
        log.info("开始检测粮情报警, recordId={}", record.getId());

        // 获取所有启用的阈值
        List<AlarmThreshold> thresholds = alarmThresholdRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlarmThreshold>()
                        .eq(AlarmThreshold::getStatus, 1));

        for (AlarmThreshold threshold : thresholds) {
            BigDecimal metricValue = getMetricValue(record, threshold.getMetricType());
            if (metricValue == null) {
                continue;
            }

            boolean triggered = checkThreshold(metricValue, threshold.getOperator(), threshold.getThresholdValue());

            if (triggered) {
                // 创建报警记录
                String alarmContent = buildAlarmContent(threshold, metricValue);
                alarmRecordService.createAlarm(
                        record.getWarehouseId(),
                        record.getBinId(),
                        record.getId(),
                        threshold.getId(),
                        threshold.getMetricType(),
                        metricValue,
                        threshold.getThresholdValue(),
                        threshold.getAlarmLevel(),
                        alarmContent
                );

                log.warn("粮情报警触发: {} {} {}, 实际值: {}",
                        threshold.getThresholdName(),
                        threshold.getOperator(),
                        threshold.getThresholdValue(),
                        metricValue);
            }
        }

        log.info("粮情报警检测完成, recordId={}", record.getId());
    }

    /**
     * 获取指标值
     */
    private BigDecimal getMetricValue(GrainConditionRecord record, String metricType) {
        return switch (metricType) {
            case "max_grain_temp" -> record.getMaxGrainTemp();
            case "min_grain_temp" -> record.getMinGrainTemp();
            case "avg_grain_temp" -> record.getAvgGrainTemp();
            case "co2_concentration" -> record.getCo2Concentration();
            case "o2_concentration" -> record.getO2Concentration();
            case "moisture_content" -> record.getMoistureContent();
            default -> null;
        };
    }

    /**
     * 检查是否触发阈值
     */
    private boolean checkThreshold(BigDecimal metricValue, String operator, BigDecimal thresholdValue) {
        int comparison = metricValue.compareTo(thresholdValue);
        return switch (operator) {
            case ">" -> comparison > 0;
            case "<" -> comparison < 0;
            case ">=" -> comparison >= 0;
            case "<=" -> comparison <= 0;
            default -> false;
        };
    }

    /**
     * 构建报警内容
     */
    private String buildAlarmContent(AlarmThreshold threshold, BigDecimal metricValue) {
        String metricTypeName = getMetricTypeName(threshold.getMetricType());
        return String.format("%s%s%s℃，当前值：%s℃",
                metricTypeName,
                threshold.getOperator(),
                threshold.getThresholdValue(),
                metricValue);
    }

    private String getMetricTypeName(String metricType) {
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
}