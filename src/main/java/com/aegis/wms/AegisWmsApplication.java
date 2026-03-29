package com.aegis.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aegis-WMS 神盾仓储系统启动类
 */
@SpringBootApplication
@MapperScan("com.aegis.wms.domain.*.repository")
public class AegisWmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AegisWmsApplication.class, args);
    }
}