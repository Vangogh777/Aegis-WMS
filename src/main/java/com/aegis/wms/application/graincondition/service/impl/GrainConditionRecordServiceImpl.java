package com.aegis.wms.application.graincondition.service.impl;

import com.aegis.wms.application.graincondition.dto.GrainConditionRecordDTO;
import com.aegis.wms.application.graincondition.service.GrainConditionRecordService;
import com.aegis.wms.application.graincondition.vo.GrainConditionRecordVO;
import com.aegis.wms.application.graincondition.vo.GrainConditionTrendVO;
import com.aegis.wms.common.event.GrainConditionRecordedEvent;
import com.aegis.wms.common.result.PageResult;
import com.aegis.wms.common.util.OrderNoGenerator;
import com.aegis.wms.domain.graincondition.entity.GrainConditionAttachment;
import com.aegis.wms.domain.graincondition.entity.GrainConditionRecord;
import com.aegis.wms.domain.graincondition.repository.GrainConditionAttachmentRepository;
import com.aegis.wms.domain.graincondition.repository.GrainConditionRecordRepository;
import com.aegis.wms.domain.masterdata.entity.Bin;
import com.aegis.wms.domain.masterdata.entity.Warehouse;
import com.aegis.wms.domain.masterdata.repository.BinRepository;
import com.aegis.wms.domain.masterdata.repository.WarehouseRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 粮情记录服务实现
 */
@Slf4j
@Service
public class GrainConditionRecordServiceImpl implements GrainConditionRecordService {

    private final GrainConditionRecordRepository recordRepository;
    private final GrainConditionAttachmentRepository attachmentRepository;
    private final WarehouseRepository warehouseRepository;
    private final BinRepository binRepository;

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Kafka Topic: 粮情已录入事件
     */
    private static final String TOPIC_GRAIN_CONDITION_RECORDED = "topic_grain_condition_recorded";

    public GrainConditionRecordServiceImpl(
            GrainConditionRecordRepository recordRepository,
            GrainConditionAttachmentRepository attachmentRepository,
            WarehouseRepository warehouseRepository,
            BinRepository binRepository) {
        this.recordRepository = recordRepository;
        this.attachmentRepository = attachmentRepository;
        this.warehouseRepository = warehouseRepository;
        this.binRepository = binRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(GrainConditionRecordDTO dto) {
        // 参数校验
        validateDTO(dto);

        // 创建粮情记录
        GrainConditionRecord record = new GrainConditionRecord();
        record.setRecordNo(generateRecordNo());
        record.setWarehouseId(dto.getWarehouseId());
        record.setBinId(dto.getBinId());
        record.setRecordDate(dto.getRecordDate());

        // 环境温湿度
        record.setOuterTemp(dto.getOuterTemp());
        record.setOuterHumidity(dto.getOuterHumidity());
        record.setInnerTemp(dto.getInnerTemp());
        record.setInnerHumidity(dto.getInnerHumidity());

        // 粮温信息
        record.setMaxGrainTemp(dto.getMaxGrainTemp());
        record.setMinGrainTemp(dto.getMinGrainTemp());
        record.setAvgGrainTemp(dto.getAvgGrainTemp());

        // 虫情气体
        record.setInsectLevel(dto.getInsectLevel());
        record.setInsectDesc(dto.getInsectDesc());
        record.setO2Concentration(dto.getO2Concentration());
        record.setCo2Concentration(dto.getCo2Concentration());

        // 其他信息
        record.setMoistureContent(dto.getMoistureContent());
        record.setRemark(dto.getRemark());
        record.setDataSource(dto.getDataSource() != null ? dto.getDataSource() : "manual");
        record.setOperatorId(1L); // TODO: 从上下文获取
        record.setOperatorName("系统");

        recordRepository.insert(record);

        // 保存附件
        if (dto.getAttachments() != null && !dto.getAttachments().isEmpty()) {
            for (GrainConditionRecordDTO.AttachmentDTO attachmentDTO : dto.getAttachments()) {
                GrainConditionAttachment attachment = new GrainConditionAttachment();
                attachment.setRecordId(record.getId());
                attachment.setFileName(attachmentDTO.getFileName());
                attachment.setFilePath(attachmentDTO.getFilePath());
                attachment.setFileType(attachmentDTO.getFileType());
                attachment.setFileSize(attachmentDTO.getFileSize());
                attachmentRepository.insert(attachment);
            }
        }

        // 发送Kafka消息，异步触发报警检测
        sendKafkaEvent(record);

        log.info("粮情记录创建成功，记录编号: {}", record.getRecordNo());

        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(GrainConditionRecordDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("记录ID不能为空");
        }

        GrainConditionRecord record = recordRepository.selectById(dto.getId());
        if (record == null) {
            throw new RuntimeException("粮情记录不存在: " + dto.getId());
        }

        // 更新字段
        if (dto.getOuterTemp() != null) record.setOuterTemp(dto.getOuterTemp());
        if (dto.getOuterHumidity() != null) record.setOuterHumidity(dto.getOuterHumidity());
        if (dto.getInnerTemp() != null) record.setInnerTemp(dto.getInnerTemp());
        if (dto.getInnerHumidity() != null) record.setInnerHumidity(dto.getInnerHumidity());
        if (dto.getMaxGrainTemp() != null) record.setMaxGrainTemp(dto.getMaxGrainTemp());
        if (dto.getMinGrainTemp() != null) record.setMinGrainTemp(dto.getMinGrainTemp());
        if (dto.getAvgGrainTemp() != null) record.setAvgGrainTemp(dto.getAvgGrainTemp());
        if (dto.getInsectLevel() != null) record.setInsectLevel(dto.getInsectLevel());
        if (dto.getInsectDesc() != null) record.setInsectDesc(dto.getInsectDesc());
        if (dto.getO2Concentration() != null) record.setO2Concentration(dto.getO2Concentration());
        if (dto.getCo2Concentration() != null) record.setCo2Concentration(dto.getCo2Concentration());
        if (dto.getMoistureContent() != null) record.setMoistureContent(dto.getMoistureContent());
        if (dto.getRemark() != null) record.setRemark(dto.getRemark());

        return recordRepository.updateById(record) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        // 删除附件
        LambdaQueryWrapper<GrainConditionAttachment> attachmentWrapper = new LambdaQueryWrapper<>();
        attachmentWrapper.eq(GrainConditionAttachment::getRecordId, id);
        attachmentRepository.delete(attachmentWrapper);

        return recordRepository.deleteById(id) > 0;
    }

    @Override
    public GrainConditionRecordVO getById(Long id) {
        GrainConditionRecord record = recordRepository.selectById(id);
        return convertToVO(record);
    }

    @Override
    public PageResult<GrainConditionRecordVO> page(Page<GrainConditionRecordVO> page, Long warehouseId, Long binId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<GrainConditionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, GrainConditionRecord::getWarehouseId, warehouseId);
        wrapper.eq(binId != null, GrainConditionRecord::getBinId, binId);
        wrapper.ge(startDate != null, GrainConditionRecord::getRecordDate, startDate);
        wrapper.le(endDate != null, GrainConditionRecord::getRecordDate, endDate);
        wrapper.orderByDesc(GrainConditionRecord::getRecordDate);

        Page<GrainConditionRecord> entityPage = new Page<>(page.getCurrent(), page.getSize());
        entityPage = recordRepository.selectPage(entityPage, wrapper);

        List<GrainConditionRecordVO> voList = convertToVOList(entityPage.getRecords());
        return PageResult.of(voList, entityPage.getTotal(), entityPage.getCurrent(), entityPage.getSize());
    }

    @Override
    public List<GrainConditionTrendVO> getTrend(Long binId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<GrainConditionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrainConditionRecord::getBinId, binId);
        wrapper.ge(startDate != null, GrainConditionRecord::getRecordDate, startDate);
        wrapper.le(endDate != null, GrainConditionRecord::getRecordDate, endDate);
        wrapper.orderByAsc(GrainConditionRecord::getRecordDate);

        List<GrainConditionRecord> records = recordRepository.selectList(wrapper);

        return records.stream().map(record -> {
            GrainConditionTrendVO vo = new GrainConditionTrendVO();
            vo.setRecordDate(record.getRecordDate());
            vo.setOuterTemp(record.getOuterTemp());
            vo.setInnerTemp(record.getInnerTemp());
            vo.setAvgGrainTemp(record.getAvgGrainTemp());
            vo.setMaxGrainTemp(record.getMaxGrainTemp());
            vo.setMinGrainTemp(record.getMinGrainTemp());
            vo.setOuterHumidity(record.getOuterHumidity());
            vo.setInnerHumidity(record.getInnerHumidity());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public GrainConditionRecordVO getLatestByBinId(Long binId) {
        LambdaQueryWrapper<GrainConditionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrainConditionRecord::getBinId, binId);
        wrapper.orderByDesc(GrainConditionRecord::getRecordDate);
        wrapper.last("LIMIT 1");
        GrainConditionRecord record = recordRepository.selectOne(wrapper);
        return convertToVO(record);
    }

    private void validateDTO(GrainConditionRecordDTO dto) {
        if (dto.getWarehouseId() == null) {
            throw new RuntimeException("库区ID不能为空");
        }
        if (dto.getBinId() == null) {
            throw new RuntimeException("仓房ID不能为空");
        }
        if (dto.getRecordDate() == null) {
            throw new RuntimeException("查仓日期不能为空");
        }

        // 校验仓房是否存在
        Bin bin = binRepository.selectById(dto.getBinId());
        if (bin == null) {
            throw new RuntimeException("仓房不存在");
        }
    }

    private String generateRecordNo() {
        return "GC" + System.currentTimeMillis();
    }

    private void sendKafkaEvent(GrainConditionRecord record) {
        if (kafkaTemplate == null) {
            log.debug("KafkaTemplate未配置，跳过发送事件");
            return;
        }
        try {
            GrainConditionRecordedEvent event = new GrainConditionRecordedEvent();
            event.setRecordId(record.getId());
            event.setRecordNo(record.getRecordNo());
            event.setWarehouseId(record.getWarehouseId());
            event.setBinId(record.getBinId());
            event.setRecordDate(record.getRecordDate());
            event.setMaxGrainTemp(record.getMaxGrainTemp());
            event.setMinGrainTemp(record.getMinGrainTemp());
            event.setAvgGrainTemp(record.getAvgGrainTemp());
            event.setO2Concentration(record.getO2Concentration());
            event.setCo2Concentration(record.getCo2Concentration());
            event.setDataSource(record.getDataSource());
            event.setEventTime(System.currentTimeMillis());

            kafkaTemplate.send(TOPIC_GRAIN_CONDITION_RECORDED, event);
            log.debug("发送粮情录入事件: recordId={}", record.getId());
        } catch (Exception e) {
            log.error("发送Kafka消息失败", e);
            // 不影响主流程
        }
    }

    private List<GrainConditionRecordVO> convertToVOList(List<GrainConditionRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }

        List<Long> warehouseIds = records.stream().map(GrainConditionRecord::getWarehouseId).distinct().collect(Collectors.toList());
        List<Long> binIds = records.stream().map(GrainConditionRecord::getBinId).distinct().collect(Collectors.toList());
        List<Long> recordIds = records.stream().map(GrainConditionRecord::getId).collect(Collectors.toList());

        Map<Long, String> warehouseNameMap = warehouseRepository.selectBatchIds(warehouseIds)
                .stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));
        Map<Long, Bin> binMap = binRepository.selectBatchIds(binIds)
                .stream().collect(Collectors.toMap(Bin::getId, b -> b));

        // 查询附件
        LambdaQueryWrapper<GrainConditionAttachment> attachmentWrapper = new LambdaQueryWrapper<>();
        attachmentWrapper.in(GrainConditionAttachment::getRecordId, recordIds);
        List<GrainConditionAttachment> allAttachments = attachmentRepository.selectList(attachmentWrapper);
        Map<Long, List<GrainConditionAttachment>> attachmentMap = allAttachments.stream()
                .collect(Collectors.groupingBy(GrainConditionAttachment::getRecordId));

        return records.stream()
                .map(record -> {
                    Bin bin = binMap.get(record.getBinId());
                    List<GrainConditionAttachment> attachments = attachmentMap.getOrDefault(record.getId(), List.of());

                    return convertToVO(record,
                            warehouseNameMap.get(record.getWarehouseId()),
                            bin != null ? bin.getBinCode() : null,
                            bin != null ? bin.getBinName() : null,
                            attachments);
                })
                .collect(Collectors.toList());
    }

    private GrainConditionRecordVO convertToVO(GrainConditionRecord record) {
        if (record == null) {
            return null;
        }
        Warehouse warehouse = warehouseRepository.selectById(record.getWarehouseId());
        Bin bin = binRepository.selectById(record.getBinId());

        LambdaQueryWrapper<GrainConditionAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrainConditionAttachment::getRecordId, record.getId());
        List<GrainConditionAttachment> attachments = attachmentRepository.selectList(wrapper);

        return convertToVO(record,
                warehouse != null ? warehouse.getWarehouseName() : null,
                bin != null ? bin.getBinCode() : null,
                bin != null ? bin.getBinName() : null,
                attachments);
    }

    private GrainConditionRecordVO convertToVO(GrainConditionRecord record, String warehouseName,
                                                 String binCode, String binName,
                                                 List<GrainConditionAttachment> attachments) {
        GrainConditionRecordVO vo = new GrainConditionRecordVO();
        vo.setId(record.getId());
        vo.setRecordNo(record.getRecordNo());
        vo.setWarehouseId(record.getWarehouseId());
        vo.setWarehouseName(warehouseName);
        vo.setBinId(record.getBinId());
        vo.setBinCode(binCode);
        vo.setBinName(binName);
        vo.setRecordDate(record.getRecordDate());
        vo.setOuterTemp(record.getOuterTemp());
        vo.setOuterHumidity(record.getOuterHumidity());
        vo.setInnerTemp(record.getInnerTemp());
        vo.setInnerHumidity(record.getInnerHumidity());
        vo.setMaxGrainTemp(record.getMaxGrainTemp());
        vo.setMinGrainTemp(record.getMinGrainTemp());
        vo.setAvgGrainTemp(record.getAvgGrainTemp());
        vo.setInsectLevel(record.getInsectLevel());
        vo.setInsectLevelName(getInsectLevelName(record.getInsectLevel()));
        vo.setInsectDesc(record.getInsectDesc());
        vo.setO2Concentration(record.getO2Concentration());
        vo.setCo2Concentration(record.getCo2Concentration());
        vo.setMoistureContent(record.getMoistureContent());
        vo.setRemark(record.getRemark());
        vo.setDataSource(record.getDataSource());
        vo.setOperatorId(record.getOperatorId());
        vo.setOperatorName(record.getOperatorName());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());

        // 附件
        if (attachments != null && !attachments.isEmpty()) {
            List<GrainConditionRecordVO.AttachmentVO> attachmentVOs = attachments.stream()
                    .map(a -> {
                        GrainConditionRecordVO.AttachmentVO avo = new GrainConditionRecordVO.AttachmentVO();
                        avo.setId(a.getId());
                        avo.setFileName(a.getFileName());
                        avo.setFilePath(a.getFilePath());
                        avo.setFileType(a.getFileType());
                        avo.setFileSize(a.getFileSize());
                        avo.setUploadTime(a.getUploadTime());
                        return avo;
                    })
                    .collect(Collectors.toList());
            vo.setAttachments(attachmentVOs);
        }

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
}