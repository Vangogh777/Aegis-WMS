package com.aegis.wms.application.inventory.service;

import com.aegis.wms.application.inventory.dto.OutboundOrderDTO;
import com.aegis.wms.application.inventory.vo.OutboundOrderVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 出库单服务接口
 */
public interface OutboundOrderService {

    /**
     * 创建出库单并执行出库操作
     * 包含分布式锁、乐观锁、幂等性校验、库存校验
     *
     * @param dto 出库单DTO
     * @return 出库单ID
     */
    Long create(OutboundOrderDTO dto);

    /**
     * 取消出库单
     *
     * @param id 出库单ID
     * @return 是否成功
     */
    Boolean cancel(Long id);

    /**
     * 根据ID查询出库单
     *
     * @param id 出库单ID
     * @return 出库单VO
     */
    OutboundOrderVO getById(Long id);

    /**
     * 分页查询出库单
     *
     * @param page        分页参数
     * @param warehouseId 库区ID
     * @param binId       仓房ID
     * @param status      状态
     * @return 分页结果
     */
    PageResult<OutboundOrderVO> page(Page<OutboundOrderVO> page, Long warehouseId, Long binId, Integer status);

    /**
     * 查询货位的出库记录
     *
     * @param positionId 货位ID
     * @return 出库单列表
     */
    List<OutboundOrderVO> listByPositionId(Long positionId);
}