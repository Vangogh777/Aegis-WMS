package com.aegis.wms.application.graincondition.service;

import com.aegis.wms.application.graincondition.dto.GrainConditionRecordDTO;
import com.aegis.wms.application.graincondition.vo.GrainConditionRecordVO;
import com.aegis.wms.application.graincondition.vo.GrainConditionTrendVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDate;
import java.util.List;

/**
 * 粮情记录服务接口
 */
public interface GrainConditionRecordService {

    /**
     * 创建粮情记录
     * 录入后发送Kafka消息触发异步报警检测
     *
     * @param dto 粮情记录DTO
     * @return 记录ID
     */
    Long create(GrainConditionRecordDTO dto);

    /**
     * 更新粮情记录
     *
     * @param dto 粮情记录DTO
     * @return 是否成功
     */
    Boolean update(GrainConditionRecordDTO dto);

    /**
     * 删除粮情记录
     *
     * @param id 记录ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 根据ID查询粮情记录
     *
     * @param id 记录ID
     * @return 粮情记录VO
     */
    GrainConditionRecordVO getById(Long id);

    /**
     * 分页查询粮情记录
     *
     * @param page        分页参数
     * @param warehouseId 库区ID
     * @param binId       仓房ID
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 分页结果
     */
    PageResult<GrainConditionRecordVO> page(Page<GrainConditionRecordVO> page, Long warehouseId, Long binId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询仓房的粮情历史趋势
     * 用于三温比对折线图
     *
     * @param binId     仓房ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据列表
     */
    List<GrainConditionTrendVO> getTrend(Long binId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询仓房最新粮情记录
     *
     * @param binId 仓房ID
     * @return 粮情记录VO
     */
    GrainConditionRecordVO getLatestByBinId(Long binId);
}