package com.aegis.wms.application.graincondition.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 粮情记录VO
 */
@Data
public class GrainConditionRecordVO {

    private Long id;

    private String recordNo;

    private Long warehouseId;

    private String warehouseName;

    private Long binId;

    private String binCode;

    private String binName;

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

    private String insectLevelName;

    private String insectDesc;

    private BigDecimal o2Concentration;

    private BigDecimal co2Concentration;

    // 其他信息
    private BigDecimal moistureContent;

    private String remark;

    private String dataSource;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 附件列表
     */
    private List<AttachmentVO> attachments;

    @Data
    public static class AttachmentVO {
        private Long id;
        private String fileName;
        private String filePath;
        private String fileType;
        private Long fileSize;
        private LocalDateTime uploadTime;
    }
}