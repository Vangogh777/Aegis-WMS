package com.aegis.wms.application.graincondition.service;

import com.aegis.wms.domain.graincondition.entity.GrainConditionRecord;

/**
 * 报警检测服务接口
 * Kafka消费者调用，异步检测报警
 */
public interface AlarmCheckService {

    /**
     * 检测粮情记录是否触发报警
     * 比对各阈值，生成报警记录
     *
     * @param record 粮情记录
     */
    void checkAndCreateAlarms(GrainConditionRecord record);
}