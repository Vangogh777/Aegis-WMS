package com.aegis.wms;

import com.aegis.wms.application.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationInitializer implements CommandLineRunner {

    private final AuthService authService;

    @Override
    public void run(String... args) {
        log.info("开始初始化系统...");
        // 初始化默认管理员账号
        authService.initDefaultAdmin();
        log.info("系统初始化完成");
    }
}