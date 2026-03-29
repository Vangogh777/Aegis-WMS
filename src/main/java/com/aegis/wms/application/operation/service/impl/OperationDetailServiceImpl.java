package com.aegis.wms.application.operation.service.impl;

import com.aegis.wms.application.operation.dto.OperationDetailDTO;
import com.aegis.wms.application.operation.service.OperationDetailService;
import com.aegis.wms.application.operation.vo.OperationDetailVO;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.domain.operation.entity.OperationDetail;
import com.aegis.wms.domain.operation.entity.OperationRecord;
import com.aegis.wms.domain.operation.repository.OperationDetailRepository;
import com.aegis.wms.domain.operation.repository.OperationRecordRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 作业明细服务实现
 */
@Service
@RequiredArgsConstructor
public class OperationDetailServiceImpl implements OperationDetailService {

    private final OperationDetailRepository operationDetailRepository;
    private final OperationRecordRepository operationRecordRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long start(OperationDetailDTO dto) {
        // 校验作业记录
        OperationRecord record = operationRecordRepository.selectById(dto.getRecordId());
        if (record == null) {
            throw new RuntimeException("作业记录不存在: " + dto.getRecordId());
        }

        // 只有作业中状态才能启动明细
        if (record.getStatus() != OperationRecord.STATUS_IN_PROGRESS) {
            throw new RuntimeException("只有作业中状态的记录才能启动启停明细");
        }

        // 检查是否已有进行中的明细
        OperationDetailVO inProgress = getInProgressDetail(dto.getRecordId());
        if (inProgress != null) {
            throw new RuntimeException("已存在进行中的启停明细，请先停止后再启动新的");
        }

        // 获取下一个序号
        Integer seqNo = getNextSeqNo(dto.getRecordId());

        OperationDetail detail = new OperationDetail();
        detail.setRecordId(dto.getRecordId());
        detail.setSeqNo(seqNo);
        detail.setStartTime(dto.getStartTime() != null ? dto.getStartTime() : LocalDateTime.now());
        detail.setStatus(OperationDetail.STATUS_IN_PROGRESS);

        // 设置开始状态
        detail.setStartAvgTemp(dto.getStartAvgTemp());
        detail.setStartMoisture(dto.getStartMoisture());
        detail.setStartInnerTemp(dto.getStartInnerTemp());
        detail.setStartInnerHumidity(dto.getStartInnerHumidity());
        detail.setStartOuterTemp(dto.getStartOuterTemp());
        detail.setStartOuterHumidity(dto.getStartOuterHumidity());
        detail.setStartMeterReading(dto.getStartMeterReading());

        // 操作人信息
        detail.setOperatorId(dto.getOperatorId());
        detail.setOperatorName(dto.getOperatorName());
        detail.setRemark(dto.getRemark());

        operationDetailRepository.insert(detail);

        // 更新作业记录的启停次数
        record.setDetailCount(seqNo);
        operationRecordRepository.updateById(record);

        return detail.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stop(OperationDetailDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("明细ID不能为空");
        }

        OperationDetail detail = operationDetailRepository.selectById(dto.getId());
        if (detail == null) {
            throw new RuntimeException("作业明细不存在: " + dto.getId());
        }

        // 只有进行中状态才能停止
        if (detail.getStatus() != OperationDetail.STATUS_IN_PROGRESS) {
            throw new RuntimeException("只有进行中状态的明细才能停止");
        }

        LocalDateTime endTime = dto.getEndTime() != null ? dto.getEndTime() : LocalDateTime.now();
        detail.setEndTime(endTime);

        // 计算时长
        if (detail.getStartTime() != null) {
            long minutes = Duration.between(detail.getStartTime(), endTime).toMinutes();
            detail.setDurationMinutes((int) minutes);
        }

        // 设置结束状态
        detail.setEndAvgTemp(dto.getEndAvgTemp());
        detail.setEndMoisture(dto.getEndMoisture());
        detail.setEndInnerTemp(dto.getEndInnerTemp());
        detail.setEndInnerHumidity(dto.getEndInnerHumidity());
        detail.setEndOuterTemp(dto.getEndOuterTemp());
        detail.setEndOuterHumidity(dto.getEndOuterHumidity());
        detail.setEndMeterReading(dto.getEndMeterReading());

        // 计算变化量
        if (detail.getStartAvgTemp() != null && detail.getEndAvgTemp() != null) {
            detail.setTempChange(detail.getStartAvgTemp().subtract(detail.getEndAvgTemp()));
        }
        if (detail.getStartMoisture() != null && detail.getEndMoisture() != null) {
            detail.setMoistureChange(detail.getStartMoisture().subtract(detail.getEndMoisture()));
        }

        // 计算耗电量
        if (detail.getStartMeterReading() != null && detail.getEndMeterReading() != null) {
            BigDecimal power = detail.getEndMeterReading().subtract(detail.getStartMeterReading());
            detail.setPowerConsumption(power.setScale(2, RoundingMode.HALF_UP));
        } else if (dto.getPowerConsumption() != null) {
            detail.setPowerConsumption(dto.getPowerConsumption());
        }

        detail.setStatus(OperationDetail.STATUS_COMPLETED);
        if (dto.getOperatorId() != null) {
            detail.setOperatorId(dto.getOperatorId());
        }
        if (dto.getOperatorName() != null) {
            detail.setOperatorName(dto.getOperatorName());
        }
        if (dto.getRemark() != null) {
            detail.setRemark(dto.getRemark());
        }

        return operationDetailRepository.updateById(detail) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(Long id) {
        OperationDetail detail = operationDetailRepository.selectById(id);
        if (detail == null) {
            throw new RuntimeException("作业明细不存在: " + id);
        }

        // 只有进行中状态才能取消
        if (detail.getStatus() != OperationDetail.STATUS_IN_PROGRESS) {
            throw new RuntimeException("只有进行中状态的明细才能取消");
        }

        detail.setStatus(OperationDetail.STATUS_CANCELLED);
        detail.setEndTime(LocalDateTime.now());

        return operationDetailRepository.updateById(detail) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(OperationDetailDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("明细ID不能为空");
        }

        OperationDetail detail = operationDetailRepository.selectById(dto.getId());
        if (detail == null) {
            throw new RuntimeException("作业明细不存在: " + dto.getId());
        }

        // 只有已完成的明细可以修改(用于数据修正)
        if (detail.getStatus() != OperationDetail.STATUS_COMPLETED) {
            throw new RuntimeException("只有已完成的明细才能修改");
        }

        // 更新各字段
        if (dto.getStartTime() != null) {
            detail.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            detail.setEndTime(dto.getEndTime());
        }
        if (dto.getDurationMinutes() != null) {
            detail.setDurationMinutes(dto.getDurationMinutes());
        }

        // 更新温湿度数据
        if (dto.getStartAvgTemp() != null) {
            detail.setStartAvgTemp(dto.getStartAvgTemp());
        }
        if (dto.getEndAvgTemp() != null) {
            detail.setEndAvgTemp(dto.getEndAvgTemp());
        }
        // 其他字段类似...

        // 重新计算变化量
        if (detail.getStartAvgTemp() != null && detail.getEndAvgTemp() != null) {
            detail.setTempChange(detail.getStartAvgTemp().subtract(detail.getEndAvgTemp()));
        }

        return operationDetailRepository.updateById(detail) > 0;
    }

    @Override
    public OperationDetailVO getById(Long id) {
        OperationDetail detail = operationDetailRepository.selectById(id);
        return convertToVO(detail);
    }

    @Override
    public List<OperationDetailVO> listByRecordId(Long recordId) {
        LambdaQueryWrapper<OperationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationDetail::getRecordId, recordId);
        wrapper.orderByAsc(OperationDetail::getSeqNo);
        List<OperationDetail> list = operationDetailRepository.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<OperationDetailVO> page(Page<OperationDetailVO> page, Long recordId, Integer status) {
        LambdaQueryWrapper<OperationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(recordId != null, OperationDetail::getRecordId, recordId);
        wrapper.eq(status != null, OperationDetail::getStatus, status);
        wrapper.orderByAsc(OperationDetail::getSeqNo);

        Page<OperationDetail> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = operationDetailRepository.selectPage(entityPage, wrapper);

        List<OperationDetailVO> voList = entityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    @Override
    public Integer getNextSeqNo(Long recordId) {
        LambdaQueryWrapper<OperationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationDetail::getRecordId, recordId);
        Long count = operationDetailRepository.selectCount(wrapper);
        return count.intValue() + 1;
    }

    @Override
    public OperationDetailVO getInProgressDetail(Long recordId) {
        LambdaQueryWrapper<OperationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationDetail::getRecordId, recordId);
        wrapper.eq(OperationDetail::getStatus, OperationDetail.STATUS_IN_PROGRESS);
        wrapper.last("LIMIT 1");
        OperationDetail detail = operationDetailRepository.selectOne(wrapper);
        return convertToVO(detail);
    }

    /**
     * 实体转VO
     */
    private OperationDetailVO convertToVO(OperationDetail detail) {
        if (detail == null) {
            return null;
        }
        OperationDetailVO vo = new OperationDetailVO();
        vo.setId(detail.getId());
        vo.setRecordId(detail.getRecordId());
        vo.setSeqNo(detail.getSeqNo());
        vo.setStartTime(detail.getStartTime());
        vo.setEndTime(detail.getEndTime());
        vo.setDurationMinutes(detail.getDurationMinutes());
        vo.setDurationDisplay(formatDuration(detail.getDurationMinutes()));
        vo.setStartMeterReading(detail.getStartMeterReading());
        vo.setEndMeterReading(detail.getEndMeterReading());
        vo.setPowerConsumption(detail.getPowerConsumption());

        // 开始状态
        vo.setStartAvgTemp(detail.getStartAvgTemp());
        vo.setStartMoisture(detail.getStartMoisture());
        vo.setStartInnerTemp(detail.getStartInnerTemp());
        vo.setStartInnerHumidity(detail.getStartInnerHumidity());
        vo.setStartOuterTemp(detail.getStartOuterTemp());
        vo.setStartOuterHumidity(detail.getStartOuterHumidity());

        // 结束状态
        vo.setEndAvgTemp(detail.getEndAvgTemp());
        vo.setEndMoisture(detail.getEndMoisture());
        vo.setEndInnerTemp(detail.getEndInnerTemp());
        vo.setEndInnerHumidity(detail.getEndInnerHumidity());
        vo.setEndOuterTemp(detail.getEndOuterTemp());
        vo.setEndOuterHumidity(detail.getEndOuterHumidity());

        // 变化量
        vo.setTempChange(detail.getTempChange());
        vo.setMoistureChange(detail.getMoistureChange());

        vo.setStatus(detail.getStatus());
        vo.setStatusName(getStatusName(detail.getStatus()));
        vo.setOperatorId(detail.getOperatorId());
        vo.setOperatorName(detail.getOperatorName());
        vo.setRemark(detail.getRemark());
        vo.setCreateTime(detail.getCreateTime());
        vo.setUpdateTime(detail.getUpdateTime());
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
            case OperationDetail.STATUS_IN_PROGRESS:
                return "进行中";
            case OperationDetail.STATUS_COMPLETED:
                return "已完成";
            case OperationDetail.STATUS_CANCELLED:
                return "已取消";
            default:
                return "未知";
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