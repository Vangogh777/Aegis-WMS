package com.aegis.wms.application.inventory.service;

import com.aegis.wms.application.inventory.vo.StockVO;
import com.aegis.wms.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存服务接口
 */
public interface StockService {

    /**
     * 查询货位库存
     *
     * @param positionId 货位ID
     * @return 库存VO
     */
    StockVO getByPositionId(Long positionId);

    /**
     * 查询仓房下所有货位库存
     *
     * @param binId 仓房ID
     * @return 库存列表
     */
    List<StockVO> listByBinId(Long binId);

    /**
     * 查询库区下所有货位库存
     *
     * @param warehouseId 库区ID
     * @return 库存列表
     */
    List<StockVO> listByWarehouseId(Long warehouseId);

    /**
     * 分页查询库存
     *
     * @param page        分页参数
     * @param warehouseId 库区ID
     * @param binId       仓房ID
     * @return 分页结果
     */
    PageResult<StockVO> page(Page<StockVO> page, Long warehouseId, Long binId);

    /**
     * 库存入库(增加库存)
     * 包含分布式锁和乐观锁机制
     *
     * @param positionId      货位ID
     * @param grainVarietyId  粮食品种ID
     * @param harvestYear     收获年份
     * @param grade           粮食等级
     * @param quantity        入库数量
     * @param orderType       单据类型
     * @param orderId         单据ID
     * @param operatorId      操作人ID
     * @param operatorName    操作人姓名
     * @param idempotentKey   幂等键
     * @return 库存变动后数量
     */
    BigDecimal inbound(Long positionId, Long grainVarietyId, Integer harvestYear, String grade,
                       BigDecimal quantity, String orderType, Long orderId,
                       Long operatorId, String operatorName, String idempotentKey);

    /**
     * 库存出库(减少库存)
     * 包含分布式锁和乐观锁机制，防止超扣
     *
     * @param positionId    货位ID
     * @param quantity      出库数量
     * @param orderType     单据类型
     * @param orderId       单据ID
     * @param operatorId    操作人ID
     * @param operatorName  操作人姓名
     * @param idempotentKey 幂等键
     * @return 库存变动后数量
     */
    BigDecimal outbound(Long positionId, BigDecimal quantity, String orderType, Long orderId,
                        Long operatorId, String operatorName, String idempotentKey);

    /**
     * 计算库区库存汇总
     *
     * @param warehouseId 库区ID
     * @return 总库存量
     */
    BigDecimal sumByWarehouseId(Long warehouseId);

    /**
     * 计算仓房库存汇总
     *
     * @param binId 仓房ID
     * @return 总库存量
     */
    BigDecimal sumByBinId(Long binId);
}