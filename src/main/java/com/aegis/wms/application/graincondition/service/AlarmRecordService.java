package com.aegis.wms.application.graincondition.service;

import com.aegis.wms.application.graincondition.dto.AlarmHandleDTO;
import com.aegis.wms.application.graincondition.vo.AlarmRecordVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 报警记录服务接口
 */
public interface AlarmRecordService {

    /**
     * 分页查询报警记录
     *
     * @param page        分页参数
     * @param warehouseId 库区ID
     * @param binId       仓房ID
     * @param status      状态
     * @return 分页结果
     */
    PageResult<AlarmRecordVO> page(Page<AlarmRecordVO> page, Long warehouseId, Long binId, Integer status);

    /**
     * 根据ID查询报警记录
     *
     * @param id 报警ID
     * @return 报警记录VO
     */
    AlarmRecordVO getById(Long id);

    /**
     * 处理报警
     *
     * @param dto 处理DTO
     * @return 是否成功
     */
    Boolean handle(AlarmHandleDTO dto);

    /**
     * 查询未处理的报警数量
     *
     * @param warehouseId 库区ID(可选)
     * @return 数量
     */
    Long countUnhandled(Long warehouseId);

    /**
     * 查询未处理的报警列表
     *
     * @param warehouseId 库区ID
     * @return 报警列表
     */
    List<AlarmRecordVO> listUnhandled(Long warehouseId);

    /**
     * 创建报警记录(内部调用)
     *
     * @param warehouseId   库区ID
     * @param binId         仓房ID
     * @param recordId      粮情记录ID
     * @param thresholdId   阈值ID
     * @param metricType    指标类型
     * @param metricValue   实际值
     * @param thresholdValue 阈值
     * @param alarmLevel    报警级别
     * @param alarmContent  报警内容
     * @return 报警ID
     */
    Long createAlarm(Long warehouseId, Long binId, Long recordId, Long thresholdId,
                     String metricType, java.math.BigDecimal metricValue, java.math.BigDecimal thresholdValue,
                     Integer alarmLevel, String alarmContent);
}