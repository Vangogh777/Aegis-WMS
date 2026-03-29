package com.aegis.wms.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 单号生成器
 */
public class OrderNoGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    /**
     * 生成入库单号
     * 格式: RK + 日期 + 6位序号
     */
    public static String generateInboundNo() {
        return "RK" + generateNo();
    }

    /**
     * 生成出库单号
     * 格式: CK + 日期 + 6位序号
     */
    public static String generateOutboundNo() {
        return "CK" + generateNo();
    }

    /**
     * 生成库存变动流水号
     * 格式: SM + 日期时间 + 6位序号
     */
    public static String generateMovementNo() {
        String dateStr = LocalDateTime.now().format(DATETIME_FORMAT);
        long seq = SEQUENCE.incrementAndGet() % 1000000;
        return "SM" + dateStr + String.format("%06d", seq);
    }

    private static String generateNo() {
        String dateStr = LocalDateTime.now().format(DATE_FORMAT);
        long seq = SEQUENCE.incrementAndGet() % 1000000;
        return dateStr + String.format("%06d", seq);
    }
}