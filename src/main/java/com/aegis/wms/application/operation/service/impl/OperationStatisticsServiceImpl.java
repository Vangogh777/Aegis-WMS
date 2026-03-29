package com.aegis.wms.application.operation.service.impl;

import com.aegis.wms.application.operation.service.OperationStatisticsService;
import com.aegis.wms.application.operation.vo.OperationStatisticsVO;
import com.aegis.wms.domain.operation.entity.OperationDetail;
import com.aegis.wms.domain.operation.entity.OperationRecord;
import com.aegis.wms.domain.operation.repository.OperationDetailRepository;
import com.aegis.wms.domain.operation.repository.OperationRecordRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作业统计服务实现
 */
@Service
@RequiredArgsConstructor
public class OperationStatisticsServiceImpl implements OperationStatisticsService {

    private final OperationRecordRepository operationRecordRepository;
    private final OperationDetailRepository operationDetailRepository;

    @Override
    public OperationStatisticsVO statisticsByWarehouse(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OperationRecord> wrapper = buildQueryWrapper(null, warehouseId, null, null, startDate, endDate);
        List<OperationRecord> records = operationRecordRepository.selectList(wrapper);

        OperationStatisticsVO vo = new OperationStatisticsVO();
        vo.setDimensionType("warehouse");
        vo.setDimensionId(warehouseId);
        vo.setDimensionName("库区-" + warehouseId);

        calculateStatistics(vo, records);

        // 添加按类型分组统计
        vo.setTypeStatisticsList(summaryByType(warehouseId, startDate, endDate));

        return vo;
    }

    @Override
    public OperationStatisticsVO statisticsByBin(Long binId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OperationRecord> wrapper = buildQueryWrapper(null, null, binId, null, startDate, endDate);
        List<OperationRecord> records = operationRecordRepository.selectList(wrapper);

        OperationStatisticsVO vo = new OperationStatisticsVO();
        vo.setDimensionType("bin");
        vo.setDimensionId(binId);
        vo.setDimensionName("仓房-" + binId);

        calculateStatistics(vo, records);

        // 添加按类型分组统计
        LambdaQueryWrapper<OperationRecord> typeWrapper = buildQueryWrapper(null, null, binId, null, startDate, endDate);
        List<OperationRecord> typeRecords = operationRecordRepository.selectList(typeWrapper);
        vo.setTypeStatisticsList(calculateTypeStatistics(typeRecords));

        return vo;
    }

    @Override
    public OperationStatisticsVO statisticsByType(String operationType, Long warehouseId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OperationRecord> wrapper = buildQueryWrapper(operationType, warehouseId, null, null, startDate, endDate);
        List<OperationRecord> records = operationRecordRepository.selectList(wrapper);

        OperationStatisticsVO vo = new OperationStatisticsVO();
        vo.setDimensionType("type");
        vo.setOperationType(operationType);
        vo.setOperationTypeName(getOperationTypeName(operationType));

        calculateStatistics(vo, records);

        return vo;
    }

    @Override
    public List<OperationStatisticsVO.TypeStatistics> summaryByType(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OperationRecord> wrapper = buildQueryWrapper(null, warehouseId, null, null, startDate, endDate);
        List<OperationRecord> records = operationRecordRepository.selectList(wrapper);

        return calculateTypeStatistics(records);
    }

    @Override
    public OperationStatisticsVO calculateRecordSummary(Long recordId) {
        OperationRecord record = operationRecordRepository.selectById(recordId);
        if (record == null) {
            return null;
        }

        OperationStatisticsVO vo = new OperationStatisticsVO();
        vo.setDimensionType("record");
        vo.setDimensionId(recordId);
        vo.setDimensionName(record.getRecordNo());
        vo.setOperationType(record.getOperationType());
        vo.setOperationTypeName(getOperationTypeName(record.getOperationType()));

        // 设置单条记录的数据
        vo.setTotalCount(1);
        switch (record.getStatus()) {
            case OperationRecord.STATUS_PENDING:
                vo.setPendingCount(1);
                break;
            case OperationRecord.STATUS_IN_PROGRESS:
                vo.setInProgressCount(1);
                break;
            case OperationRecord.STATUS_COMPLETED:
                vo.setCompletedCount(1);
                break;
            case OperationRecord.STATUS_CANCELLED:
                vo.setCancelledCount(1);
                break;
        }

        vo.setTotalDurationMinutes(record.getTotalDurationMinutes());
        vo.setTotalDurationDisplay(formatDuration(record.getTotalDurationMinutes()));
        vo.setTotalPowerConsumption(record.getTotalPower());
        vo.setTotalDetailCount(record.getDetailCount());
        vo.setAvgTempDrop(record.getTempDrop());
        vo.setAvgMoistureDrop(record.getMoistureDrop());

        return vo;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<OperationRecord> buildQueryWrapper(String operationType, Long warehouseId,
                                                                   Long binId, Integer status,
                                                                   LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OperationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(operationType != null, OperationRecord::getOperationType, operationType);
        wrapper.eq(warehouseId != null, OperationRecord::getWarehouseId, warehouseId);
        wrapper.eq(binId != null, OperationRecord::getBinId, binId);
        wrapper.eq(status != null, OperationRecord::getStatus, status);

        if (startDate != null) {
            wrapper.ge(OperationRecord::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(OperationRecord::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }

        return wrapper;
    }

    /**
     * 计算统计数据
     */
    private void calculateStatistics(OperationStatisticsVO vo, List<OperationRecord> records) {
        if (records.isEmpty()) {
            vo.setTotalCount(0);
            vo.setPendingCount(0);
            vo.setInProgressCount(0);
            vo.setCompletedCount(0);
            vo.setCancelledCount(0);
            return;
        }

        // 数量统计
        vo.setTotalCount(records.size());
        vo.setPendingCount((int) records.stream().filter(r -> r.getStatus() == OperationRecord.STATUS_PENDING).count());
        vo.setInProgressCount((int) records.stream().filter(r -> r.getStatus() == OperationRecord.STATUS_IN_PROGRESS).count());
        vo.setCompletedCount((int) records.stream().filter(r -> r.getStatus() == OperationRecord.STATUS_COMPLETED).count());
        vo.setCancelledCount((int) records.stream().filter(r -> r.getStatus() == OperationRecord.STATUS_CANCELLED).count());

        // 只统计已完成的记录
        List<OperationRecord> completedRecords = records.stream()
                .filter(r -> r.getStatus() == OperationRecord.STATUS_COMPLETED)
                .collect(Collectors.toList());

        if (completedRecords.isEmpty()) {
            return;
        }

        // 时长统计
        List<Integer> durations = completedRecords.stream()
                .filter(r -> r.getTotalDurationMinutes() != null)
                .map(OperationRecord::getTotalDurationMinutes)
                .collect(Collectors.toList());

        if (!durations.isEmpty()) {
            int totalDuration = durations.stream().mapToInt(Integer::intValue).sum();
            vo.setTotalDurationMinutes(totalDuration);
            vo.setTotalDurationDisplay(formatDuration(totalDuration));
            vo.setAvgDurationMinutes(BigDecimal.valueOf(totalDuration / durations.size())
                    .setScale(2, RoundingMode.HALF_UP));
            vo.setMaxDurationMinutes(durations.stream().mapToInt(Integer::intValue).max().orElse(0));
            vo.setMinDurationMinutes(durations.stream().mapToInt(Integer::intValue).min().orElse(0));
        }

        // 能耗统计
        List<BigDecimal> powers = completedRecords.stream()
                .filter(r -> r.getTotalPower() != null)
                .map(OperationRecord::getTotalPower)
                .collect(Collectors.toList());

        if (!powers.isEmpty()) {
            BigDecimal totalPower = powers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setTotalPowerConsumption(totalPower.setScale(2, RoundingMode.HALF_UP));
            vo.setAvgPowerConsumption(totalPower.divide(BigDecimal.valueOf(powers.size()), 2, RoundingMode.HALF_UP));
        }

        // 温度变化统计
        List<BigDecimal> tempDrops = completedRecords.stream()
                .filter(r -> r.getTempDrop() != null)
                .map(OperationRecord::getTempDrop)
                .collect(Collectors.toList());

        if (!tempDrops.isEmpty()) {
            BigDecimal avgTempDrop = tempDrops.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(tempDrops.size()), 2, RoundingMode.HALF_UP);
            vo.setAvgTempDrop(avgTempDrop);
        }

        // 水分变化统计
        List<BigDecimal> moistureDrops = completedRecords.stream()
                .filter(r -> r.getMoistureDrop() != null)
                .map(OperationRecord::getMoistureDrop)
                .collect(Collectors.toList());

        if (!moistureDrops.isEmpty()) {
            BigDecimal avgMoistureDrop = moistureDrops.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(moistureDrops.size()), 2, RoundingMode.HALF_UP);
            vo.setAvgMoistureDrop(avgMoistureDrop);
        }

        // 启停次数统计
        List<Integer> detailCounts = completedRecords.stream()
                .filter(r -> r.getDetailCount() != null)
                .map(OperationRecord::getDetailCount)
                .collect(Collectors.toList());

        if (!detailCounts.isEmpty()) {
            int totalDetails = detailCounts.stream().mapToInt(Integer::intValue).sum();
            vo.setTotalDetailCount(totalDetails);
            vo.setAvgDetailCount(BigDecimal.valueOf(totalDetails / detailCounts.size())
                    .setScale(2, RoundingMode.HALF_UP));
        }
    }

    /**
     * 按类型统计
     */
    private List<OperationStatisticsVO.TypeStatistics> calculateTypeStatistics(List<OperationRecord> records) {
        Map<String, List<OperationRecord>> typeMap = records.stream()
                .collect(Collectors.groupingBy(OperationRecord::getOperationType));

        List<OperationStatisticsVO.TypeStatistics> result = new ArrayList<>();
        for (Map.Entry<String, List<OperationRecord>> entry : typeMap.entrySet()) {
            OperationStatisticsVO.TypeStatistics typeStats = new OperationStatisticsVO.TypeStatistics();
            typeStats.setOperationType(entry.getKey());
            typeStats.setOperationTypeName(getOperationTypeName(entry.getKey()));
            typeStats.setCount(entry.getValue().size());

            List<OperationRecord> typeRecords = entry.getValue().stream()
                    .filter(r -> r.getStatus() == OperationRecord.STATUS_COMPLETED)
                    .collect(Collectors.toList());

            if (!typeRecords.isEmpty()) {
                int totalDuration = typeRecords.stream()
                        .filter(r -> r.getTotalDurationMinutes() != null)
                        .mapToInt(OperationRecord::getTotalDurationMinutes)
                        .sum();
                typeStats.setTotalDurationMinutes(totalDuration);

                BigDecimal totalPower = typeRecords.stream()
                        .filter(r -> r.getTotalPower() != null)
                        .map(OperationRecord::getTotalPower)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                typeStats.setTotalPowerConsumption(totalPower.setScale(2, RoundingMode.HALF_UP));

                List<BigDecimal> tempDrops = typeRecords.stream()
                        .filter(r -> r.getTempDrop() != null)
                        .map(OperationRecord::getTempDrop)
                        .collect(Collectors.toList());
                if (!tempDrops.isEmpty()) {
                    BigDecimal avgTempDrop = tempDrops.stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(tempDrops.size()), 2, RoundingMode.HALF_UP);
                    typeStats.setAvgTempDrop(avgTempDrop);
                }
            }

            result.add(typeStats);
        }

        return result;
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

    /**
     * 格式化时长
     */
    private String formatDuration(Integer minutes) {
        if (minutes == null || minutes == 0) {
            return "0分钟";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0 && mins > 0) {
            return hours + "小时" + mins + "分钟";
        } else if (hours > 0) {
            return hours + "小时";
        } else {
            return mins + "分钟";
        }
    }
}