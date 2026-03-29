package com.aegis.wms.application.graincondition.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 粮情记录DTO
 */
@Data
public class GrainConditionRecordDTO {

    private Long id;

    private Long warehouseId;

    private Long binId;

    private LocalDate recordDate;

    // 环境温湿度
    private BigDecimal outerTemp;

    private BigDecimal outerHumidity;

    private BigDecimal innerTemp;

    private BigDecimal innerHumidity;

    // 粮温信息
    private BigDecimal maxGrainTemp;

    private BigDecimal minGrainTemp;

    private BigDecimal avgGrainTemp;

    // 虫情气体
    private String insectLevel;

    private String insectDesc;

    private BigDecimal o2Concentration;

    private BigDecimal co2Concentration;

    // 其他信息
    private BigDecimal moistureContent;

    private String remark;

    /**
     * 数据来源: manual-手工录入 iot-传感器采集
     */
    private String dataSource;

    /**
     * 附件列表
     */
    private List<AttachmentDTO> attachments;

    /**
     * 附件DTO
     */
    @Data
    public static class AttachmentDTO {
        private String fileName;
        private String filePath;
        private String fileType;
        private Long fileSize;
    }
}