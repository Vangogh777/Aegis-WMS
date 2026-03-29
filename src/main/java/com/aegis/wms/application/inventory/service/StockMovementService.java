package com.aegis.wms.application.inventory.service;

import com.aegis.wms.application.inventory.vo.StockMovementVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 库存变动流水服务接口
 */
public interface StockMovementService {

    /**
     * 查询货位的变动流水
     *
     * @param positionId 货位ID
     * @return 流水列表
     */
    List<StockMovementVO> listByPositionId(Long positionId);

    /**
     * 分页查询变动流水
     *
     * @param page        分页参数
     * @param warehouseId 库区ID
     * @param binId       仓房ID
     * @param positionId  货位ID
     * @param movementType 变动类型
     * @return 分页结果
     */
    PageResult<StockMovementVO> page(Page<StockMovementVO> page, Long warehouseId, Long binId, Long positionId, Integer movementType);
}