package com.aegis.wms.application.operation.service;

import com.aegis.wms.application.operation.dto.OperationDetailDTO;
import com.aegis.wms.application.operation.vo.OperationDetailVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 作业明细服务接口
 */
public interface OperationDetailService {

    /**
     * 启动作业明细(开始一次启停记录)
     *
     * @param dto 作业明细DTO(包含recordId, 开始时间, 开始状态信息)
     * @return 明细ID
     */
    Long start(OperationDetailDTO dto);

    /**
     * 停止作业明细(结束一次启停记录)
     *
     * @param dto 作业明细DTO(包含id, 结束时间, 结束状态信息)
     * @return 是否成功
     */
    Boolean stop(OperationDetailDTO dto);

    /**
     * 取消作业明细
     *
     * @param id 明细ID
     * @return 是否成功
     */
    Boolean cancel(Long id);

    /**
     * 更新作业明细
     *
     * @param dto 作业明细DTO
     * @return 是否成功
     */
    Boolean update(OperationDetailDTO dto);

    /**
     * 根据ID查询作业明细
     *
     * @param id 明细ID
     * @return 明细VO
     */
    OperationDetailVO getById(Long id);

    /**
     * 查询作业记录的所有明细
     *
     * @param recordId 记录ID
     * @return 明细列表
     */
    List<OperationDetailVO> listByRecordId(Long recordId);

    /**
     * 分页查询作业明细
     *
     * @param page 分页参数
     * @param recordId 记录ID
     * @param status 状态
     * @return 分页结果
     */
    PageResult<OperationDetailVO> page(Page<OperationDetailVO> page, Long recordId, Integer status);

    /**
     * 获取作业记录的下一个序号
     *
     * @param recordId 记录ID
     * @return 下一个序号
     */
    Integer getNextSeqNo(Long recordId);

    /**
     * 获取作业记录当前进行中的明细
     *
     * @param recordId 记录ID
     * @return 进行中的明细，如果没有返回null
     */
    OperationDetailVO getInProgressDetail(Long recordId);
}