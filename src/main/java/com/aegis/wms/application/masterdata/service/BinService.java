package com.aegis.wms.application.masterdata.service;

import com.aegis.wms.application.masterdata.dto.BinDTO;
import com.aegis.wms.application.masterdata.vo.BinVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 仓房应用服务接口
 */
public interface BinService {

    /**
     * 创建仓房
     *
     * @param dto 仓房DTO
     * @return 仓房ID
     */
    Long create(BinDTO dto);

    /**
     * 更新仓房
     *
     * @param dto 仓房DTO
     * @return 是否成功
     */
    Boolean update(BinDTO dto);

    /**
     * 删除仓房(逻辑删除)
     *
     * @param id 仓房ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 根据ID查询仓房
     *
     * @param id 仓房ID
     * @return 仓房VO
     */
    BinVO getById(Long id);

    /**
     * 根据库区ID查询仓房列表
     *
     * @param warehouseId 库区ID
     * @return 仓房列表
     */
    List<BinVO> listByWarehouseId(Long warehouseId);

    /**
     * 查询所有仓房列表
     *
     * @return 仓房列表
     */
    List<BinVO> listAll();

    /**
     * 分页查询仓房
     *
     * @param page 分页参数
     * @param warehouseId 库区ID
     * @param binName 仓房名称(模糊查询)
     * @param status 状态
     * @return 分页结果
     */
    PageResult<BinVO> page(Page<BinVO> page, Long warehouseId, String binName, Integer status);
}