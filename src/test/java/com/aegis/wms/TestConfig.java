package com.aegis.wms;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 测试配置 - 禁用Kafka自动配置
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class TestConfig {
}