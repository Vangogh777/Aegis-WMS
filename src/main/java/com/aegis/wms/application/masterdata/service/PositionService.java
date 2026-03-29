package com.aegis.wms.application.masterdata.service;

import com.aegis.wms.application.masterdata.dto.PositionDTO;
import com.aegis.wms.application.masterdata.vo.PositionVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 货位应用服务接口
 */
public interface PositionService {

    /**
     * 创建货位
     *
     * @param dto 货位DTO
     * @return 货位ID
     */
    Long create(PositionDTO dto);

    /**
     * 更新货位
     *
     * @param dto 货位DTO
     * @return 是否成功
     */
    Boolean update(PositionDTO dto);

    /**
     * 删除货位(逻辑删除)
     *
     * @param id 货位ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 根据ID查询货位
     *
     * @param id 货位ID
     * @return 货位VO
     */
    PositionVO getById(Long id);

    /**
     * 根据仓房ID查询货位列表
     *
     * @param binId 仓房ID
     * @return 货位列表
     */
    List<PositionVO> listByBinId(Long binId);

    /**
     * 分页查询货位
     *
     * @param page 分页参数
     * @param binId 仓房ID
     * @param positionName 货位名称(模糊查询)
     * @param status 状态
     * @return 分页结果
     */
    PageResult<PositionVO> page(Page<PositionVO> page, Long binId, String positionName, Integer status);

    /**
     * 级联选择查询：库区 -> 仓房 -> 货位
     * 用于出入库单存储位置选择
     *
     * @param warehouseId 库区ID
     * @param binId 仓房ID(可选)
     * @return 货位列表(带仓房和库区信息)
     */
    List<PositionVO> listCascade(Long warehouseId, Long binId);
}