package com.aegis.wms.application.inventory.service;

import com.aegis.wms.application.inventory.dto.InboundOrderDTO;
import com.aegis.wms.application.inventory.vo.InboundOrderVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 入库单服务接口
 */
public interface InboundOrderService {

    /**
     * 创建入库单并执行入库操作
     * 包含分布式锁、乐观锁、幂等性校验
     *
     * @param dto 入库单DTO
     * @return 入库单ID
     */
    Long create(InboundOrderDTO dto);

    /**
     * 取消入库单
     *
     * @param id 入库单ID
     * @return 是否成功
     */
    Boolean cancel(Long id);

    /**
     * 根据ID查询入库单
     *
     * @param id 入库单ID
     * @return 入库单VO
     */
    InboundOrderVO getById(Long id);

    /**
     * 分页查询入库单
     *
     * @param page        分页参数
     * @param warehouseId 库区ID
     * @param binId       仓房ID
     * @param status      状态
     * @return 分页结果
     */
    PageResult<InboundOrderVO> page(Page<InboundOrderVO> page, Long warehouseId, Long binId, Integer status);

    /**
     * 查询货位的入库记录
     *
     * @param positionId 货位ID
     * @return 入库单列表
     */
    List<InboundOrderVO> listByPositionId(Long positionId);
}