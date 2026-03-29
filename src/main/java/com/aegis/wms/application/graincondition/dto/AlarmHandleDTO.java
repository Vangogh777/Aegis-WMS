package com.aegis.wms.application.graincondition.dto;

import lombok.Data;

/**
 * 报警处理DTO
 */
@Data
public class AlarmHandleDTO {

    private Long alarmId;

    /**
     * 处理状态: 1-已确认 2-已处理
     */
    private Integer status;

    private String handleRemark;
}