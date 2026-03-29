package com.aegis.wms.application.masterdata.service;

import com.aegis.wms.application.masterdata.dto.WarehouseDTO;
import com.aegis.wms.application.masterdata.vo.WarehouseVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 库区应用服务接口
 */
public interface WarehouseService {

    /**
     * 创建库区
     *
     * @param dto 库区DTO
     * @return 库区ID
     */
    Long create(WarehouseDTO dto);

    /**
     * 更新库区
     *
     * @param dto 库区DTO
     * @return 是否成功
     */
    Boolean update(WarehouseDTO dto);

    /**
     * 删除库区(逻辑删除)
     *
     * @param id 库区ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 根据ID查询库区
     *
     * @param id 库区ID
     * @return 库区VO
     */
    WarehouseVO getById(Long id);

    /**
     * 根据编码查询库区
     *
     * @param code 库区编码
     * @return 库区VO
     */
    WarehouseVO getByCode(String code);

    /**
     * 查询所有库区
     *
     * @return 库区列表
     */
    List<WarehouseVO> listAll();

    /**
     * 分页查询库区
     *
     * @param page 分页参数
     * @param warehouseName 库区名称(模糊查询)
     * @param status 状态
     * @return 分页结果
     */
    PageResult<WarehouseVO> page(Page<WarehouseVO> page, String warehouseName, Integer status);
}