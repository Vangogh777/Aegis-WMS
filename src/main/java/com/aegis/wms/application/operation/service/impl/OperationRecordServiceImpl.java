package com.aegis.wms.application.operation.service.impl;

import com.aegis.wms.application.operation.dto.OperationDetailDTO;
import com.aegis.wms.application.operation.dto.OperationRecordDTO;
import com.aegis.wms.application.operation.service.OperationDetailService;
import com.aegis.wms.application.operation.service.OperationRecordService;
import com.aegis.wms.application.operation.vo.OperationDetailVO;
import com.aegis.wms.application.operation.vo.OperationRecordVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.util.OrderNoGenerator;
import com.aegis.wms.domain.operation.entity.OperationDetail;
import com.aegis.wms.domain.operation.entity.OperationRecord;
import com.aegis.wms.domain.operation.repository.OperationDetailRepository;
import com.aegis.wms.domain.operation.repository.OperationRecordRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 作业记录服务实现
 */
@Service
@RequiredArgsConstructor
public class OperationRecordServiceImpl implements OperationRecordService {

    private final OperationRecordRepository operationRecordRepository;
    private final OperationDetailRepository operationDetailRepository;
    private final OperationDetailService operationDetailService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OperationRecordDTO dto) {
        OperationRecord record = new OperationRecord();
        record.setRecordNo(generateRecordNo());
        record.setSchemeId(dto.getSchemeId());
        record.setWarehouseId(dto.getWarehouseId());
        record.setBinId(dto.getBinId());
        record.setOperationType(dto.getOperationType());
        record.setStatus(OperationRecord.STATUS_PENDING);
        record.setOperatorId(dto.getOperatorId());
        record.setOperatorName(dto.getOperatorName());
        record.setRemark(dto.getRemark());
        record.setDetailCount(0);

        operationRecordRepository.insert(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(OperationRecordDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("记录ID不能为空");
        }

        OperationRecord record = operationRecordRepository.selectById(dto.getId());
        if (record == null) {
            throw new RuntimeException("作业记录不存在: " + dto.getId());
        }

        // 只有待执行状态才能修改基本信息
        if (record.getStatus() != OperationRecord.STATUS_PENDING) {
            throw new RuntimeException("只有待执行状态的记录才能修改基本信息");
        }

        if (dto.getSchemeId() != null) {
            record.setSchemeId(dto.getSchemeId());
        }
        if (dto.getWarehouseId() != null) {
            record.setWarehouseId(dto.getWarehouseId());
        }
        if (dto.getBinId() != null) {
            record.setBinId(dto.getBinId());
        }
        if (StringUtils.hasText(dto.getOperationType())) {
            record.setOperationType(dto.getOperationType());
        }
        if (StringUtils.hasText(dto.getRemark())) {
            record.setRemark(dto.getRemark());
        }

        return operationRecordRepository.updateById(record) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        OperationRecord record = operationRecordRepository.selectById(id);
        if (record == null) {
            throw new RuntimeException("作业记录不存在: " + id);
        }

        // 只有待执行或已取消状态才能删除
        if (record.getStatus() != OperationRecord.STATUS_PENDING &&
            record.getStatus() != OperationRecord.STATUS_CANCELLED) {
            throw new RuntimeException("只有待执行或已取消状态的记录才能删除");
        }

        // 删除关联的明细
        LambdaQueryWrapper<OperationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationDetail::getRecordId, id);
        operationDetailRepository.delete(wrapper);

        return operationRecordRepository.deleteById(id) > 0;
    }

    @Override
    public OperationRecordVO getById(Long id) {
        OperationRecord record = operationRecordRepository.selectById(id);
        OperationRecordVO vo = convertToVO(record);
        if (vo != null) {
            // 加载明细列表
            List<OperationDetailVO> details = operationDetailService.listByRecordId(id);
            vo.setDetails(details);
        }
        return vo;
    }

    @Override
    public OperationRecordVO getByRecordNo(String recordNo) {
        LambdaQueryWrapper<OperationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationRecord::getRecordNo, recordNo);
        OperationRecord record = operationRecordRepository.selectOne(wrapper);
        OperationRecordVO vo = convertToVO(record);
        if (vo != null) {
            List<OperationDetailVO> details = operationDetailService.listByRecordId(record.getId());
            vo.setDetails(details);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean startOperation(Long id, Long operatorId, String operatorName) {
        OperationRecord record = operationRecordRepository.selectById(id);
        if (record == null) {
            throw new RuntimeException("作业记录不存在: " + id);
        }

        // 状态校验: 只有待执行状态可以启动
        if (record.getStatus() != OperationRecord.STATUS_PENDING) {
            throw new RuntimeException("只有待执行状态的记录才能启动作业");
        }

        record.setStatus(OperationRecord.STATUS_IN_PROGRESS);
        record.setStartTime(LocalDateTime.now());
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);

        return operationRecordRepository.updateById(record) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean completeOperation(Long id, Long operatorId, String operatorName) {
        OperationRecord record = operationRecordRepository.selectById(id);
        if (record == null) {
            throw new RuntimeException("作业记录不存在: " + id);
        }

        // 状态校验: 只有作业中状态可以完成
        if (record.getStatus() != OperationRecord.STATUS_IN_PROGRESS) {
            throw new RuntimeException("只有作业中状态的记录才能完成作业");
        }

        // 检查是否有进行中的明细
        OperationDetailVO inProgressDetail = operationDetailService.getInProgressDetail(id);
        if (inProgressDetail != null) {
            throw new RuntimeException("存在进行中的启停明细，请先停止后再完成作业");
        }

        record.setStatus(OperationRecord.STATUS_COMPLETED);
        record.setEndTime(LocalDateTime.now());
        if (operatorId != null) {
            record.setOperatorId(operatorId);
        }
        if (StringUtils.hasText(operatorName)) {
            record.setOperatorName(operatorName);
        }

        // 汇总数据
        summarize(id);

        return operationRecordRepository.updateById(record) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOperation(Long id, Long operatorId, String operatorName, String cancelReason) {
        OperationRecord record = operationRecordRepository.selectById(id);
        if (record == null) {
            throw new RuntimeException("作业记录不存在: " + id);
        }

        // 状态校验: 只有待执行或作业中状态可以取消
        if (record.getStatus() != OperationRecord.STATUS_PENDING &&
            record.getStatus() != OperationRecord.STATUS_IN_PROGRESS) {
            throw new RuntimeException("只有待执行或作业中状态的记录才能取消");
        }

        // 如果有进行中的明细，先取消
        OperationDetailVO inProgressDetail = operationDetailService.getInProgressDetail(id);
        if (inProgressDetail != null) {
            operationDetailService.cancel(inProgressDetail.getId());
        }

        record.setStatus(OperationRecord.STATUS_CANCELLED);
        record.setEndTime(LocalDateTime.now());
        if (operatorId != null) {
            record.setOperatorId(operatorId);
        }
        if (StringUtils.hasText(operatorName)) {
            record.setOperatorName(operatorName);
        }
        if (StringUtils.hasText(cancelReason)) {
            record.setRemark(cancelReason);
        }

        return operationRecordRepository.updateById(record) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean summarize(Long id) {
        OperationRecord record = operationRecordRepository.selectById(id);
        if (record == null) {
            return false;
        }

        // 获取所有已完成的明细
        LambdaQueryWrapper<OperationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationDetail::getRecordId, id);
        wrapper.eq(OperationDetail::getStatus, OperationDetail.STATUS_COMPLETED);
        wrapper.orderByAsc(OperationDetail::getSeqNo);
        List<OperationDetail> details = operationDetailRepository.selectList(wrapper);

        if (details.isEmpty()) {
            return true;
        }

        // 计算累计时长
        int totalDuration = details.stream()
                .filter(d -> d.getDurationMinutes() != null)
                .mapToInt(OperationDetail::getDurationMinutes)
                .sum();

        // 计算累计耗电量
        BigDecimal totalPower = details.stream()
                .filter(d -> d.getPowerConsumption() != null)
                .map(OperationDetail::getPowerConsumption)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 设置起始状态(第一条明细的开始状态)
        OperationDetail firstDetail = details.get(0);
        record.setStartAvgTemp(firstDetail.getStartAvgTemp());
        record.setStartMoisture(firstDetail.getStartMoisture());

        // 设置结束状态(最后一条明细的结束状态)
        OperationDetail lastDetail = details.get(details.size() - 1);
        record.setEndAvgTemp(lastDetail.getEndAvgTemp());
        record.setEndMoisture(lastDetail.getEndMoisture());

        // 计算变化量
        if (record.getStartAvgTemp() != null && record.getEndAvgTemp() != null) {
            record.setTempDrop(record.getStartAvgTemp().subtract(record.getEndAvgTemp()));
        }
        if (record.getStartMoisture() != null && record.getEndMoisture() != null) {
            record.setMoistureDrop(record.getStartMoisture().subtract(record.getEndMoisture()));
        }

        // 更新汇总数据
        record.setTotalDurationMinutes(totalDuration);
        record.setTotalPower(totalPower.setScale(2, RoundingMode.HALF_UP));
        record.setDetailCount(details.size());

        return operationRecordRepository.updateById(record) > 0;
    }

    @Override
    public PageResult<OperationRecordVO> page(Page<OperationRecordVO> page, Long warehouseId, Long binId,
                                              String operationType, Integer status, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OperationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, OperationRecord::getWarehouseId, warehouseId);
        wrapper.eq(binId != null, OperationRecord::getBinId, binId);
        wrapper.eq(StringUtils.hasText(operationType), OperationRecord::getOperationType, operationType);
        wrapper.eq(status != null, OperationRecord::getStatus, status);

        if (startDate != null) {
            wrapper.ge(OperationRecord::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(OperationRecord::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }

        wrapper.orderByDesc(OperationRecord::getCreateTime);

        Page<OperationRecord> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = operationRecordRepository.selectPage(entityPage, wrapper);

        List<OperationRecordVO> voList = entityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    @Override
    public List<OperationRecordVO> listByBinId(Long binId) {
        LambdaQueryWrapper<OperationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationRecord::getBinId, binId);
        wrapper.orderByDesc(OperationRecord::getCreateTime);
        List<OperationRecord> list = operationRecordRepository.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<OperationRecordVO> listByWarehouseId(Long warehouseId) {
        LambdaQueryWrapper<OperationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationRecord::getWarehouseId, warehouseId);
        wrapper.orderByDesc(OperationRecord::getCreateTime);
        List<OperationRecord> list = operationRecordRepository.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 生成记录编号
     */
    private String generateRecordNo() {
        String dateStr = LocalDateTime.now().format(DATE_FORMAT);
        long seq = System.currentTimeMillis() % 1000000;
        return "OP" + dateStr + String.format("%06d", seq);
    }

    /**
     * 实体转VO
     */
    private OperationRecordVO convertToVO(OperationRecord record) {
        if (record == null) {
            return null;
        }
        OperationRecordVO vo = new OperationRecordVO();
        vo.setId(record.getId());
        vo.setRecordNo(record.getRecordNo());
        vo.setSchemeId(record.getSchemeId());
        vo.setWarehouseId(record.getWarehouseId());
        vo.setBinId(record.getBinId());
        vo.setOperationType(record.getOperationType());
        vo.setOperationTypeName(getOperationTypeName(record.getOperationType()));
        vo.setStatus(record.getStatus());
        vo.setStatusName(getStatusName(record.getStatus()));
        vo.setStartTime(record.getStartTime());
        vo.setEndTime(record.getEndTime());
        vo.setTotalDurationMinutes(record.getTotalDurationMinutes());
        vo.setTotalDurationDisplay(formatDuration(record.getTotalDurationMinutes()));
        vo.setTotalPower(record.getTotalPower());
        vo.setDetailCount(record.getDetailCount());
        vo.setStartAvgTemp(record.getStartAvgTemp());
        vo.setStartMoisture(record.getStartMoisture());
        vo.setEndAvgTemp(record.getEndAvgTemp());
        vo.setEndMoisture(record.getEndMoisture());
        vo.setTempDrop(record.getTempDrop());
        vo.setMoistureDrop(record.getMoistureDrop());
        vo.setOperatorId(record.getOperatorId());
        vo.setOperatorName(record.getOperatorName());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());
        return vo;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case OperationRecord.STATUS_PENDING:
                return "待执行";
            case OperationRecord.STATUS_IN_PROGRESS:
                return "作业中";
            case OperationRecord.STATUS_COMPLETED:
                return "已完成";
            case OperationRecord.STATUS_CANCELLED:
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 获取作业类型名称
     */
    private String getOperationTypeName(String operationType) {
        if (operationType == null) {
            return null;
        }
        switch (operationType) {
            case OperationRecord.TYPE_VENTILATION:
                return "通风作业";
            case OperationRecord.TYPE_AERATION:
                return "气调作业";
            case OperationRecord.TYPE_TEMPERATURE:
                return "控温作业";
            case OperationRecord.TYPE_FUMIGATION:
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