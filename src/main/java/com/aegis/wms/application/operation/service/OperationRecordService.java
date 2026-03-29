package com.aegis.wms.application.operation.service;

import com.aegis.wms.application.operation.dto.OperationRecordDTO;
import com.aegis.wms.application.operation.vo.OperationRecordVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDate;
import java.util.List;

/**
 * 作业记录服务接口
 */
public interface OperationRecordService {

    /**
     * 创建作业记录(待执行状态)
     *
     * @param dto 作业记录DTO
     * @return 记录ID
     */
    Long create(OperationRecordDTO dto);

    /**
     * 更新作业记录
     *
     * @param dto 作业记录DTO
     * @return 是否成功
     */
    Boolean update(OperationRecordDTO dto);

    /**
     * 删除作业记录
     *
     * @param id 记录ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 根据ID查询作业记录(含明细)
     *
     * @param id 记录ID
     * @return 记录VO
     */
    OperationRecordVO getById(Long id);

    /**
     * 根据编号查询作业记录
     *
     * @param recordNo 记录编号
     * @return 记录VO
     */
    OperationRecordVO getByRecordNo(String recordNo);

    /**
     * 启动作业(待执行 -> 作业中)
     *
     * @param id 记录ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 是否成功
     */
    Boolean startOperation(Long id, Long operatorId, String operatorName);

    /**
     * 完成作业(作业中 -> 已完成)
     *
     * @param id 记录ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 是否成功
     */
    Boolean completeOperation(Long id, Long operatorId, String operatorName);

    /**
     * 取消作业(待执行/作业中 -> 已取消)
     *
     * @param id 记录ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param cancelReason 取消原因
     * @return 是否成功
     */
    Boolean cancelOperation(Long id, Long operatorId, String operatorName, String cancelReason);

    /**
     * 汇总作业记录(更新汇总数据)
     *
     * @param id 记录ID
     * @return 是否成功
     */
    Boolean summarize(Long id);

    /**
     * 分页查询作业记录
     *
     * @param page 分页参数
     * @param warehouseId 库区ID
     * @param binId 仓房ID
     * @param operationType 作业类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    PageResult<OperationRecordVO> page(Page<OperationRecordVO> page, Long warehouseId, Long binId,
                                       String operationType, Integer status, LocalDate startDate, LocalDate endDate);

    /**
     * 查询仓房的作业记录列表
     *
     * @param binId 仓房ID
     * @return 记录列表
     */
    List<OperationRecordVO> listByBinId(Long binId);

    /**
     * 查询库区的作业记录列表
     *
     * @param warehouseId 库区ID
     * @return 记录列表
     */
    List<OperationRecordVO> listByWarehouseId(Long warehouseId);
}