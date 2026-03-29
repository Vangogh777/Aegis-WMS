package com.aegis.wms.domain.graincondition.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 粮情附件实体
 */
@Data
@TableName("grain_condition_attachment")
public class GrainConditionAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private String fileName;

    private String filePath;

    private String fileType;

    private Long fileSize;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
}