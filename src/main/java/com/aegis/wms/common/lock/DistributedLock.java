package com.aegis.wms.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * 用于库存高并发扣减场景
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final StringRedisTemplate redisTemplate;

    /**
     * 锁前缀
     */
    private static final String LOCK_PREFIX = "wh:inv:lock:";

    /**
     * 默认锁等待时间(秒)
     */
    private static final long DEFAULT_WAIT_TIME = 3;

    /**
     * 默认锁持有时间(秒)
     */
    private static final long DEFAULT_LEASE_TIME = 10;

    /**
     * 尝试获取分布式锁
     *
     * @param warehouseId 库区ID
     * @param binId       仓房ID
     * @param positionId  货位ID
     * @return 是否获取成功
     */
    public boolean tryLock(Long warehouseId, Long binId, Long positionId) {
        return tryLock(warehouseId, binId, positionId, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
    }

    /**
     * 尝试获取分布式锁
     *
     * @param warehouseId 库区ID
     * @param binId       仓房ID
     * @param positionId  货位ID
     * @param waitTime    等待时间(秒)
     * @param leaseTime   持有时间(秒)
     * @return 是否获取成功
     */
    public boolean tryLock(Long warehouseId, Long binId, Long positionId, long waitTime, long leaseTime) {
        String lockKey = buildLockKey(warehouseId, binId, positionId);
        long startTime = System.currentTimeMillis();

        try {
            while (true) {
                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, String.valueOf(Thread.currentThread().getId()), leaseTime, TimeUnit.SECONDS);

                if (Boolean.TRUE.equals(acquired)) {
                    log.debug("获取锁成功: {}", lockKey);
                    return true;
                }

                // 检查是否超时
                if (System.currentTimeMillis() - startTime > waitTime * 1000) {
                    log.warn("获取锁超时: {}", lockKey);
                    return false;
                }

                // 短暂休眠后重试
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param warehouseId 库区ID
     * @param binId       仓房ID
     * @param positionId  货位ID
     */
    public void unlock(Long warehouseId, Long binId, Long positionId) {
        String lockKey = buildLockKey(warehouseId, binId, positionId);
        try {
            redisTemplate.delete(lockKey);
            log.debug("释放锁成功: {}", lockKey);
        } catch (Exception e) {
            log.error("释放锁失败: {}", lockKey, e);
        }
    }

    /**
     * 构建锁Key
     * 格式: wh:inv:lock:{warehouseId}_{binId}_{positionId}
     */
    private String buildLockKey(Long warehouseId, Long binId, Long positionId) {
        return LOCK_PREFIX + warehouseId + "_" + binId + "_" + positionId;
    }
}