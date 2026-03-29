package com.aegis.wms.application.operation.service;

import com.aegis.wms.application.operation.dto.OperationSchemeDTO;
import com.aegis.wms.application.operation.vo.OperationSchemeVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 作业方案服务接口
 */
public interface OperationSchemeService {

    /**
     * 创建作业方案
     *
     * @param dto 作业方案DTO
     * @return 方案ID
     */
    Long create(OperationSchemeDTO dto);

    /**
     * 更新作业方案
     *
     * @param dto 作业方案DTO
     * @return 是否成功
     */
    Boolean update(OperationSchemeDTO dto);

    /**
     * 删除作业方案(逻辑删除)
     *
     * @param id 方案ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 根据ID查询作业方案
     *
     * @param id 方案ID
     * @return 方案VO
     */
    OperationSchemeVO getById(Long id);

    /**
     * 根据编码查询作业方案
     *
     * @param code 方案编码
     * @return 方案VO
     */
    OperationSchemeVO getByCode(String code);

    /**
     * 查询所有启用的作业方案
     *
     * @return 方案列表
     */
    List<OperationSchemeVO> listAllEnabled();

    /**
     * 根据作业类型查询方案列表
     *
     * @param operationType 作业类型
     * @return 方案列表
     */
    List<OperationSchemeVO> listByType(String operationType);

    /**
     * 分页查询作业方案
     *
     * @param page 分页参数
     * @param schemeName 方案名称(模糊查询)
     * @param operationType 作业类型
     * @param status 状态
     * @return 分页结果
     */
    PageResult<OperationSchemeVO> page(Page<OperationSchemeVO> page, String schemeName, String operationType, Integer status);
}