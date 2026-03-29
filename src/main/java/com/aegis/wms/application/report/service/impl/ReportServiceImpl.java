package com.aegis.wms.application.report.service.impl;

import com.aegis.wms.application.report.service.ReportService;
import com.aegis.wms.application.report.vo.*;
import com.aegis.wms.domain.graincondition.entity.AlarmRecord;
import com.aegis.wms.domain.graincondition.entity.GrainConditionRecord;
import com.aegis.wms.domain.graincondition.repository.AlarmRecordRepository;
import com.aegis.wms.domain.graincondition.repository.GrainConditionRecordRepository;
import com.aegis.wms.domain.inventory.entity.InboundOrder;
import com.aegis.wms.domain.inventory.entity.OutboundOrder;
import com.aegis.wms.domain.inventory.entity.Stock;
import com.aegis.wms.domain.inventory.repository.GrainVarietyRepository;
import com.aegis.wms.domain.inventory.repository.InboundOrderRepository;
import com.aegis.wms.domain.inventory.repository.OutboundOrderRepository;
import com.aegis.wms.domain.inventory.repository.StockRepository;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.masterdata.entity.Position;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.masterdata.repository.PositionRepository;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.aegis.wms.domain.operation.entity.OperationRecord;
import com.aegis.wms.domain.operation.repository.OperationRecordRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final WarehouseRepository warehouseRepository;
    private final BinRepository binRepository;
    private final PositionRepository positionRepository;
    private final StockRepository stockRepository;
    private final GrainVarietyRepository grainVarietyRepository;
    private final InboundOrderRepository inboundOrderRepository;
    private final OutboundOrderRepository outboundOrderRepository;
    private final GrainConditionRecordRepository grainConditionRecordRepository;
    private final AlarmRecordRepository alarmRecordRepository;
    private final OperationRecordRepository operationRecordRepository;

    @Override
    public WarehouseOverviewVO getWarehouseOverview(Long warehouseId) {
        Warehouse warehouse = warehouseId != null ? warehouseRepository.selectById(warehouseId) : null;
        
        LambdaQueryWrapper<Bin> binWrapper = new LambdaQueryWrapper<>();
        binWrapper.eq(warehouseId != null, Bin::getWarehouseId, warehouseId);
        List<Bin> bins = binRepository.selectList(binWrapper);
        
        List<Long> binIds = bins.stream().map(Bin::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<Position> positionWrapper = new LambdaQueryWrapper<>();
        positionWrapper.in(!binIds.isEmpty(), Position::getBinId, binIds);
        List<Position> positions = positionRepository.selectList(positionWrapper);
        
        List<Long> positionIds = positions.stream().map(Position::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<Stock> stockWrapper = new LambdaQueryWrapper<>();
        stockWrapper.in(!positionIds.isEmpty(), Stock::getPositionId, positionIds);
        List<Stock> stocks = stockRepository.selectList(stockWrapper);
        
        BigDecimal totalCapacity = warehouse != null ? warehouse.getTotalCapacity() : 
                bins.stream().map(Bin::getCapacity).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalStock = stocks.stream()
                .map(Stock::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        WarehouseOverviewVO vo = new WarehouseOverviewVO();
        if (warehouse != null) {
            vo.setWarehouseId(warehouse.getId());
            vo.setWarehouseCode(warehouse.getWarehouseCode());
            vo.setWarehouseName(warehouse.getWarehouseName());
            vo.setTotalCapacity(warehouse.getTotalCapacity());
        } else {
            vo.setTotalCapacity(totalCapacity);
        }
        
        vo.setBinCount(bins.size());
        vo.setPositionCount(positions.size());
        vo.setTotalStock(totalStock);
        
        if (totalCapacity != null && totalCapacity.compareTo(BigDecimal.ZERO) > 0) {
            vo.setUsageRate(totalStock.multiply(new BigDecimal("100"))
                    .divide(totalCapacity, 2, RoundingMode.HALF_UP));
        }
        
        // 统计空仓和满仓
        Map<Long, BigDecimal> binStockMap = new HashMap<>();
        for (Stock stock : stocks) {
            Long binId = stock.getBinId();
            binStockMap.merge(binId, stock.getQuantity() != null ? stock.getQuantity() : BigDecimal.ZERO, BigDecimal::add);
        }
        
        int emptyBinCount = 0;
        int fullBinCount = 0;
        for (Bin bin : bins) {
            BigDecimal stockQty = binStockMap.getOrDefault(bin.getId(), BigDecimal.ZERO);
            if (stockQty.compareTo(BigDecimal.ZERO) == 0) {
                emptyBinCount++;
            } else if (bin.getCapacity() != null && stockQty.compareTo(bin.getCapacity()) >= 0) {
                fullBinCount++;
            }
        }
        vo.setEmptyBinCount(emptyBinCount);
        vo.setFullBinCount(fullBinCount);
        
        return vo;
    }

    @Override
    public StockSummaryVO getStockSummary(Long warehouseId, Long binId) {
        LambdaQueryWrapper<Bin> binWrapper = new LambdaQueryWrapper<>();
        binWrapper.eq(warehouseId != null, Bin::getWarehouseId, warehouseId);
        binWrapper.eq(binId != null, Bin::getId, binId);
        List<Bin> bins = binRepository.selectList(binWrapper);
        
        List<Long> binIds = bins.stream().map(Bin::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<Position> positionWrapper = new LambdaQueryWrapper<>();
        positionWrapper.in(!binIds.isEmpty(), Position::getBinId, binIds);
        List<Position> positions = positionRepository.selectList(positionWrapper);
        
        List<Long> positionIds = positions.stream().map(Position::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<Stock> stockWrapper = new LambdaQueryWrapper<>();
        stockWrapper.in(!positionIds.isEmpty(), Stock::getPositionId, positionIds);
        List<Stock> stocks = stockRepository.selectList(stockWrapper);
        
        BigDecimal totalQuantity = stocks.stream()
                .map(Stock::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int occupiedCount = (int) stocks.stream()
                .filter(s -> s.getQuantity() != null && s.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        StockSummaryVO vo = new StockSummaryVO();
        vo.setTotalQuantity(totalQuantity);
        vo.setPositionCount(positions.size());
        vo.setOccupiedPositionCount(occupiedCount);
        
        // 按品种汇总
        Map<Long, BigDecimal> varietyStock = stocks.stream()
                .filter(s -> s.getGrainVarietyId() != null && s.getQuantity() != null)
                .collect(Collectors.groupingBy(Stock::getGrainVarietyId,
                        Collectors.reducing(BigDecimal.ZERO, Stock::getQuantity, BigDecimal::add)));
        
        List<StockSummaryVO.VarietySummary> byVariety = new ArrayList<>();
        varietyStock.forEach((varietyId, qty) -> {
            StockSummaryVO.VarietySummary summary = new StockSummaryVO.VarietySummary();
            summary.setGrainVarietyId(varietyId);
            // 获取品种名称
            var variety = grainVarietyRepository.selectById(varietyId);
            if (variety != null) {
                summary.setVarietyCode(variety.getVarietyCode());
                summary.setVarietyName(variety.getVarietyName());
            } else {
                summary.setVarietyName("未知品种");
            }
            summary.setQuantity(qty);
            if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                summary.setPercentage(qty.multiply(new BigDecimal("100"))
                        .divide(totalQuantity, 2, RoundingMode.HALF_UP));
            }
            byVariety.add(summary);
        });
        vo.setByVariety(byVariety);
        
        // 按仓房汇总
        Map<Long, BigDecimal> binStockMap = stocks.stream()
                .filter(s -> s.getBinId() != null && s.getQuantity() != null)
                .collect(Collectors.groupingBy(Stock::getBinId,
                        Collectors.reducing(BigDecimal.ZERO, Stock::getQuantity, BigDecimal::add)));
        
        Map<Long, Long> binVarietyMap = stocks.stream()
                .filter(s -> s.getBinId() != null && s.getGrainVarietyId() != null)
                .collect(Collectors.toMap(Stock::getBinId, Stock::getGrainVarietyId, (a, b) -> a));
        
        List<StockSummaryVO.BinSummary> byBin = new ArrayList<>();
        Map<Long, Bin> binMap = bins.stream().collect(Collectors.toMap(Bin::getId, b -> b));
        
        binStockMap.forEach((bId, qty) -> {
            Bin bin = binMap.get(bId);
            if (bin != null) {
                StockSummaryVO.BinSummary summary = new StockSummaryVO.BinSummary();
                summary.setBinId(bId);
                summary.setBinCode(bin.getBinCode());
                summary.setBinName(bin.getBinName());
                summary.setCapacity(bin.getCapacity());
                summary.setQuantity(qty);
                
                if (bin.getCapacity() != null && bin.getCapacity().compareTo(BigDecimal.ZERO) > 0) {
                    summary.setUsageRate(qty.multiply(new BigDecimal("100"))
                            .divide(bin.getCapacity(), 2, RoundingMode.HALF_UP));
                }
                
                Long varietyId = binVarietyMap.get(bId);
                if (varietyId != null) {
                    var variety = grainVarietyRepository.selectById(varietyId);
                    if (variety != null) {
                        summary.setGrainVarietyName(variety.getVarietyName());
                    }
                }
                byBin.add(summary);
            }
        });
        vo.setByBin(byBin);
        
        return vo;
    }

    @Override
    public GrainConditionSummaryVO getGrainConditionSummary(Long warehouseId, Long binId) {
        LambdaQueryWrapper<Bin> binWrapper = new LambdaQueryWrapper<>();
        binWrapper.eq(warehouseId != null, Bin::getWarehouseId, warehouseId);
        binWrapper.eq(binId != null, Bin::getId, binId);
        List<Bin> bins = binRepository.selectList(binWrapper);
        
        List<Long> binIds = bins.stream().map(Bin::getId).collect(Collectors.toList());
        
        // 获取未处理报警数
        LambdaQueryWrapper<AlarmRecord> alarmWrapper = new LambdaQueryWrapper<>();
        alarmWrapper.eq(warehouseId != null, AlarmRecord::getWarehouseId, warehouseId);
        alarmWrapper.in(!binIds.isEmpty(), AlarmRecord::getBinId, binIds);
        alarmWrapper.eq(AlarmRecord::getStatus, 0);
        Long pendingAlarmCount = alarmRecordRepository.selectCount(alarmWrapper);
        
        GrainConditionSummaryVO vo = new GrainConditionSummaryVO();
        vo.setTotalBins(bins.size());
        vo.setPendingAlarmCount(pendingAlarmCount.intValue());
        
        // 获取各仓房最新粮情
        List<GrainConditionSummaryVO.BinGrainCondition> binConditions = new ArrayList<>();
        BigDecimal totalAvgTemp = BigDecimal.ZERO;
        BigDecimal maxTemp = null;
        BigDecimal minTemp = null;
        int detectedCount = 0;
        BigDecimal totalInnerTemp = BigDecimal.ZERO;
        BigDecimal totalInnerHumidity = BigDecimal.ZERO;
        
        for (Bin bin : bins) {
            LambdaQueryWrapper<GrainConditionRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(GrainConditionRecord::getBinId, bin.getId());
            recordWrapper.orderByDesc(GrainConditionRecord::getRecordDate);
            recordWrapper.last("LIMIT 1");
            GrainConditionRecord record = grainConditionRecordRepository.selectOne(recordWrapper);
            
            GrainConditionSummaryVO.BinGrainCondition condition = new GrainConditionSummaryVO.BinGrainCondition();
            condition.setBinId(bin.getId());
            condition.setBinCode(bin.getBinCode());
            condition.setBinName(bin.getBinName());
            
            if (record != null) {
                detectedCount++;
                condition.setRecordDate(record.getRecordDate() != null ? record.getRecordDate().toString() : null);
                condition.setMaxGrainTemp(record.getMaxGrainTemp());
                condition.setMinGrainTemp(record.getMinGrainTemp());
                condition.setAvgGrainTemp(record.getAvgGrainTemp());
                condition.setInnerTemp(record.getInnerTemp());
                condition.setInnerHumidity(record.getInnerHumidity());
                condition.setInsectLevel(record.getInsectLevel());
                condition.setInsectLevelName(getInsectLevelName(record.getInsectLevel()));
                
                if (record.getAvgGrainTemp() != null) {
                    totalAvgTemp = totalAvgTemp.add(record.getAvgGrainTemp());
                }
                if (record.getInnerTemp() != null) {
                    totalInnerTemp = totalInnerTemp.add(record.getInnerTemp());
                }
                if (record.getInnerHumidity() != null) {
                    totalInnerHumidity = totalInnerHumidity.add(record.getInnerHumidity());
                }
                if (record.getMaxGrainTemp() != null) {
                    maxTemp = maxTemp == null ? record.getMaxGrainTemp() : maxTemp.max(record.getMaxGrainTemp());
                }
                if (record.getMinGrainTemp() != null) {
                    minTemp = minTemp == null ? record.getMinGrainTemp() : minTemp.min(record.getMinGrainTemp());
                }
                
                // 获取该仓房未处理报警数
                LambdaQueryWrapper<AlarmRecord> binAlarmWrapper = new LambdaQueryWrapper<>();
                binAlarmWrapper.eq(AlarmRecord::getBinId, bin.getId());
                binAlarmWrapper.eq(AlarmRecord::getStatus, 0);
                Long binAlarmCount = alarmRecordRepository.selectCount(binAlarmWrapper);
                condition.setAlarmCount(binAlarmCount.intValue());
                
                // 设置状态
                condition.setStatus(binAlarmCount > 0 ? "alarm" : 
                        (record.getMaxGrainTemp() != null && record.getMaxGrainTemp().compareTo(new BigDecimal("25")) > 0) ? "warning" : "normal");
            } else {
                condition.setStatus("normal");
                condition.setAlarmCount(0);
            }
            
            binConditions.add(condition);
        }
        
        vo.setDetectedBins(detectedCount);
        vo.setBinConditions(binConditions);
        vo.setMaxGrainTemp(maxTemp);
        vo.setMinGrainTemp(minTemp);
        
        if (detectedCount > 0) {
            vo.setAvgGrainTemp(totalAvgTemp.divide(new BigDecimal(detectedCount), 2, RoundingMode.HALF_UP));
            vo.setAvgInnerTemp(totalInnerTemp.divide(new BigDecimal(detectedCount), 2, RoundingMode.HALF_UP));
            vo.setAvgInnerHumidity(totalInnerHumidity.divide(new BigDecimal(detectedCount), 2, RoundingMode.HALF_UP));
        }
        
        return vo;
    }

    @Override
    public OperationSummaryVO getOperationSummary(Long warehouseId, Integer year, Integer month) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        
        LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime monthEnd = LocalDateTime.of(year, month, YearMonth.of(year, month).lengthOfMonth(), 23, 59, 59);
        
        OperationSummaryVO vo = new OperationSummaryVO();
        
        // 入库统计
        LambdaQueryWrapper<InboundOrder> inboundWrapper = new LambdaQueryWrapper<>();
        inboundWrapper.eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId);
        inboundWrapper.ge(InboundOrder::getCreateTime, monthStart);
        inboundWrapper.le(InboundOrder::getCreateTime, monthEnd);
        inboundWrapper.eq(InboundOrder::getStatus, 2); // 已完成
        List<InboundOrder> inboundOrders = inboundOrderRepository.selectList(inboundWrapper);
        
        vo.setInboundCount(inboundOrders.size());
        vo.setInboundQuantity(inboundOrders.stream()
                .map(InboundOrder::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 出库统计
        LambdaQueryWrapper<OutboundOrder> outboundWrapper = new LambdaQueryWrapper<>();
        outboundWrapper.eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId);
        outboundWrapper.ge(OutboundOrder::getCreateTime, monthStart);
        outboundWrapper.le(OutboundOrder::getCreateTime, monthEnd);
        outboundWrapper.eq(OutboundOrder::getStatus, 2);
        List<OutboundOrder> outboundOrders = outboundOrderRepository.selectList(outboundWrapper);
        
        vo.setOutboundCount(outboundOrders.size());
        vo.setOutboundQuantity(outboundOrders.stream()
                .map(OutboundOrder::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 粮情检测次数
        LambdaQueryWrapper<GrainConditionRecord> gcWrapper = new LambdaQueryWrapper<>();
        gcWrapper.eq(warehouseId != null, GrainConditionRecord::getWarehouseId, warehouseId);
        gcWrapper.ge(GrainConditionRecord::getRecordDate, monthStart.toLocalDate());
        gcWrapper.le(GrainConditionRecord::getRecordDate, monthEnd.toLocalDate());
        Long gcCount = grainConditionRecordRepository.selectCount(gcWrapper);
        vo.setGrainConditionCount(gcCount.intValue());
        
        // 报警统计
        LambdaQueryWrapper<AlarmRecord> alarmWrapper = new LambdaQueryWrapper<>();
        alarmWrapper.eq(warehouseId != null, AlarmRecord::getWarehouseId, warehouseId);
        alarmWrapper.ge(AlarmRecord::getCreateTime, monthStart);
        alarmWrapper.le(AlarmRecord::getCreateTime, monthEnd);
        List<AlarmRecord> alarms = alarmRecordRepository.selectList(alarmWrapper);
        
        vo.setAlarmCount(alarms.size());
        vo.setHandledAlarmCount((int) alarms.stream().filter(a -> a.getStatus() == 2).count());
        
        // 作业时长统计
        LambdaQueryWrapper<OperationRecord> opWrapper = new LambdaQueryWrapper<>();
        opWrapper.eq(warehouseId != null, OperationRecord::getWarehouseId, warehouseId);
        opWrapper.ge(OperationRecord::getCreateTime, monthStart);
        opWrapper.le(OperationRecord::getCreateTime, monthEnd);
        opWrapper.eq(OperationRecord::getStatus, 2); // 已完成
        List<OperationRecord> operations = operationRecordRepository.selectList(opWrapper);
        
        int totalMinutes = operations.stream()
                .filter(o -> o.getTotalDurationMinutes() != null)
                .mapToInt(OperationRecord::getTotalDurationMinutes)
                .sum();
        vo.setTotalDuration(new BigDecimal(totalMinutes).divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP));
        
        if (!operations.isEmpty()) {
            vo.setAvgDuration(vo.getTotalDuration().divide(new BigDecimal(operations.size()), 2, RoundingMode.HALF_UP));
        }
        
        return vo;
    }

    @Override
    public AlarmStatisticsVO getAlarmStatistics(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, AlarmRecord::getWarehouseId, warehouseId);
        wrapper.ge(AlarmRecord::getCreateTime, startDate.atStartOfDay());
        wrapper.le(AlarmRecord::getCreateTime, endDate.atTime(LocalTime.MAX));
        List<AlarmRecord> alarms = alarmRecordRepository.selectList(wrapper);
        
        AlarmStatisticsVO vo = new AlarmStatisticsVO();
        vo.setTotalAlarmCount(alarms.size());
        vo.setPendingCount((int) alarms.stream().filter(a -> a.getStatus() == 0).count());
        vo.setConfirmedCount((int) alarms.stream().filter(a -> a.getStatus() == 1).count());
        vo.setHandledCount((int) alarms.stream().filter(a -> a.getStatus() == 2).count());
        
        // 按级别统计
        Map<Integer, Long> levelCount = alarms.stream()
                .collect(Collectors.groupingBy(AlarmRecord::getAlarmLevel, Collectors.counting()));
        
        List<AlarmStatisticsVO.LevelStatistics> byLevel = levelCount.entrySet().stream()
                .map(e -> {
                    AlarmStatisticsVO.LevelStatistics ls = new AlarmStatisticsVO.LevelStatistics();
                    ls.setAlarmLevel(e.getKey());
                    ls.setAlarmLevelName(getAlarmLevelName(e.getKey()));
                    ls.setCount(e.getValue().intValue());
                    return ls;
                })
                .collect(Collectors.toList());
        vo.setByLevel(byLevel);
        
        // 按类型统计
        Map<String, Long> typeCount = alarms.stream()
                .filter(a -> a.getMetricType() != null)
                .collect(Collectors.groupingBy(AlarmRecord::getMetricType, Collectors.counting()));
        
        List<AlarmStatisticsVO.TypeStatistics> byType = typeCount.entrySet().stream()
                .map(e -> {
                    AlarmStatisticsVO.TypeStatistics ts = new AlarmStatisticsVO.TypeStatistics();
                    ts.setMetricType(e.getKey());
                    ts.setMetricTypeName(getMetricTypeName(e.getKey()));
                    ts.setCount(e.getValue().intValue());
                    return ts;
                })
                .collect(Collectors.toList());
        vo.setByType(byType);
        
        // 按仓房统计
        Map<Long, Long> binCount = alarms.stream()
                .filter(a -> a.getBinId() != null)
                .collect(Collectors.groupingBy(AlarmRecord::getBinId, Collectors.counting()));
        
        List<AlarmStatisticsVO.BinStatistics> byBin = new ArrayList<>();
        binCount.forEach((binId, count) -> {
            Bin bin = binRepository.selectById(binId);
            if (bin != null) {
                AlarmStatisticsVO.BinStatistics bs = new AlarmStatisticsVO.BinStatistics();
                bs.setBinId(binId);
                bs.setBinCode(bin.getBinCode());
                bs.setBinName(bin.getBinName());
                bs.setCount(count.intValue());
                byBin.add(bs);
            }
        });
        vo.setByBin(byBin);
        
        return vo;
    }

    @Override
    public InboundOutboundStatisticsVO getInboundOutboundStatistics(Long warehouseId, Integer year, Integer month) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        
        LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime monthEnd = LocalDateTime.of(year, month, YearMonth.of(year, month).lengthOfMonth(), 23, 59, 59);
        LocalDateTime yearStart = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        
        InboundOutboundStatisticsVO vo = new InboundOutboundStatisticsVO();
        
        // 本月入库
        LambdaQueryWrapper<InboundOrder> monthlyInboundWrapper = new LambdaQueryWrapper<>();
        monthlyInboundWrapper.eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId);
        monthlyInboundWrapper.ge(InboundOrder::getCreateTime, monthStart);
        monthlyInboundWrapper.le(InboundOrder::getCreateTime, monthEnd);
        monthlyInboundWrapper.eq(InboundOrder::getStatus, 2);
        List<InboundOrder> monthlyInbound = inboundOrderRepository.selectList(monthlyInboundWrapper);
        
        vo.setMonthlyInboundCount(monthlyInbound.size());
        vo.setMonthlyInboundQuantity(monthlyInbound.stream()
                .map(InboundOrder::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 本月出库
        LambdaQueryWrapper<OutboundOrder> monthlyOutboundWrapper = new LambdaQueryWrapper<>();
        monthlyOutboundWrapper.eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId);
        monthlyOutboundWrapper.ge(OutboundOrder::getCreateTime, monthStart);
        monthlyOutboundWrapper.le(OutboundOrder::getCreateTime, monthEnd);
        monthlyOutboundWrapper.eq(OutboundOrder::getStatus, 2);
        List<OutboundOrder> monthlyOutbound = outboundOrderRepository.selectList(monthlyOutboundWrapper);
        
        vo.setMonthlyOutboundCount(monthlyOutbound.size());
        vo.setMonthlyOutboundQuantity(monthlyOutbound.stream()
                .map(OutboundOrder::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 本年入库
        LambdaQueryWrapper<InboundOrder> yearlyInboundWrapper = new LambdaQueryWrapper<>();
        yearlyInboundWrapper.eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId);
        yearlyInboundWrapper.ge(InboundOrder::getCreateTime, yearStart);
        yearlyInboundWrapper.le(InboundOrder::getCreateTime, yearEnd);
        yearlyInboundWrapper.eq(InboundOrder::getStatus, 2);
        List<InboundOrder> yearlyInbound = inboundOrderRepository.selectList(yearlyInboundWrapper);
        
        vo.setYearlyInboundCount(yearlyInbound.size());
        vo.setYearlyInboundQuantity(yearlyInbound.stream()
                .map(InboundOrder::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 本年出库
        LambdaQueryWrapper<OutboundOrder> yearlyOutboundWrapper = new LambdaQueryWrapper<>();
        yearlyOutboundWrapper.eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId);
        yearlyOutboundWrapper.ge(OutboundOrder::getCreateTime, yearStart);
        yearlyOutboundWrapper.le(OutboundOrder::getCreateTime, yearEnd);
        yearlyOutboundWrapper.eq(OutboundOrder::getStatus, 2);
        List<OutboundOrder> yearlyOutbound = outboundOrderRepository.selectList(yearlyOutboundWrapper);
        
        vo.setYearlyOutboundCount(yearlyOutbound.size());
        vo.setYearlyOutboundQuantity(yearlyOutbound.stream()
                .map(OutboundOrder::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 按品种和仓房统计可以类似实现...
        vo.setInboundByVariety(new ArrayList<>());
        vo.setOutboundByVariety(new ArrayList<>());
        vo.setInboundByBin(new ArrayList<>());
        vo.setOutboundByBin(new ArrayList<>());
        
        return vo;
    }

    @Override
    public DailyReportVO getDailyReport(LocalDate date, Long warehouseId) {
        if (date == null) date = LocalDate.now();
        
        DailyReportVO vo = new DailyReportVO();
        vo.setReportDate(date);
        
        // 当日入库
        LambdaQueryWrapper<InboundOrder> inboundWrapper = new LambdaQueryWrapper<>();
        inboundWrapper.eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId);
        inboundWrapper.ge(InboundOrder::getCreateTime, date.atStartOfDay());
        inboundWrapper.le(InboundOrder::getCreateTime, date.atTime(LocalTime.MAX));
        inboundWrapper.eq(InboundOrder::getStatus, 2);
        List<InboundOrder> dailyInbound = inboundOrderRepository.selectList(inboundWrapper);
        
        vo.setInboundCount(dailyInbound.size());
        vo.setInboundQuantity(dailyInbound.stream()
                .map(InboundOrder::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 当日出库
        LambdaQueryWrapper<OutboundOrder> outboundWrapper = new LambdaQueryWrapper<>();
        outboundWrapper.eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId);
        outboundWrapper.ge(OutboundOrder::getCreateTime, date.atStartOfDay());
        outboundWrapper.le(OutboundOrder::getCreateTime, date.atTime(LocalTime.MAX));
        outboundWrapper.eq(OutboundOrder::getStatus, 2);
        List<OutboundOrder> dailyOutbound = outboundOrderRepository.selectList(outboundWrapper);
        
        vo.setOutboundCount(dailyOutbound.size());
        vo.setOutboundQuantity(dailyOutbound.stream()
                .map(OutboundOrder::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 当日粮情检测
        LambdaQueryWrapper<GrainConditionRecord> gcWrapper = new LambdaQueryWrapper<>();
        gcWrapper.eq(warehouseId != null, GrainConditionRecord::getWarehouseId, warehouseId);
        gcWrapper.eq(GrainConditionRecord::getRecordDate, date);
        List<GrainConditionRecord> dailyGC = grainConditionRecordRepository.selectList(gcWrapper);
        
        vo.setDetectedBinCount(dailyGC.size());
        
        // 当日报警
        LambdaQueryWrapper<AlarmRecord> alarmWrapper = new LambdaQueryWrapper<>();
        alarmWrapper.eq(warehouseId != null, AlarmRecord::getWarehouseId, warehouseId);
        alarmWrapper.ge(AlarmRecord::getCreateTime, date.atStartOfDay());
        alarmWrapper.le(AlarmRecord::getCreateTime, date.atTime(LocalTime.MAX));
        List<AlarmRecord> dailyAlarms = alarmRecordRepository.selectList(alarmWrapper);
        
        vo.setAlarmCount(dailyAlarms.size());
        
        return vo;
    }

    @Override
    public MonthlyReportVO getMonthlyReport(Integer year, Integer month, Long warehouseId) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        
        MonthlyReportVO vo = new MonthlyReportVO();
        vo.setYear(year);
        vo.setMonth(month);
        
        // 复用其他统计方法
        OperationSummaryVO operationSummary = getOperationSummary(warehouseId, year, month);
        vo.setInboundCount(operationSummary.getInboundCount());
        vo.setInboundQuantity(operationSummary.getInboundQuantity());
        vo.setOutboundCount(operationSummary.getOutboundCount());
        vo.setOutboundQuantity(operationSummary.getOutboundQuantity());
        vo.setDetectionCount(operationSummary.getGrainConditionCount());
        vo.setAlarmCount(operationSummary.getAlarmCount());
        
        return vo;
    }
    
    private String getInsectLevelName(String insectLevel) {
        if (insectLevel == null) return null;
        return switch (insectLevel) {
            case "NONE" -> "无虫";
            case "BASIC" -> "基本无虫";
            case "NORMAL" -> "一般虫粮";
            case "SERIOUS" -> "严重虫粮";
            default -> insectLevel;
        };
    }
    
    private String getAlarmLevelName(Integer level) {
        if (level == null) return "未知";
        return switch (level) {
            case 1 -> "一般";
            case 2 -> "严重";
            case 3 -> "紧急";
            default -> "未知";
        };
    }
    
    private String getMetricTypeName(String metricType) {
        if (metricType == null) return "未知";
        return switch (metricType) {
            case "max_grain_temp" -> "最高粮温";
            case "min_grain_temp" -> "最低粮温";
            case "avg_grain_temp" -> "平均粮温";
            case "co2_concentration" -> "CO2浓度";
            case "o2_concentration" -> "O2浓度";
            default -> metricType;
        };
    }
}
