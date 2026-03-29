package com.aegis.wms.infrastructure.kafka.consumer;

import com.aegis.wms.application.graincondition.service.AlarmCheckService;
import com.aegis.wms.common.event.GrainConditionRecordedEvent;
import com.aegis.wms.domain.graincondition.entity.GrainConditionRecord;
import com.aegis.wms.domain.graincondition.repository.GrainConditionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 粮情事件消费者
 * 监听粮情录入事件，异步触发报警检测
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrainConditionEventConsumer {

    private final AlarmCheckService alarmCheckService;
    private final GrainConditionRecordRepository recordRepository;

    /**
     * 消费粮情录入事件
     * 触发报警检测
     */
    @KafkaListener(topics = "topic_grain_condition_recorded", groupId = "alarm-check-group")
    public void onGrainConditionRecorded(ConsumerRecord<String, GrainConditionRecordedEvent> record) {
        GrainConditionRecordedEvent event = record.value();
        log.info("收到粮情录入事件, recordId={}", event.getRecordId());

        try {
            // 查询完整记录
            GrainConditionRecord grainConditionRecord = recordRepository.selectById(event.getRecordId());
            if (grainConditionRecord == null) {
                log.warn("粮情记录不存在: {}", event.getRecordId());
                return;
            }

            // 执行报警检测
            alarmCheckService.checkAndCreateAlarms(grainConditionRecord);

        } catch (Exception e) {
            log.error("处理粮情录入事件失败, recordId={}", event.getRecordId(), e);
        }
    }
}