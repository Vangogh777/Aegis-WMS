package com.aegis.wms.application.inventory.service;

import com.aegis.wms.application.inventory.dto.GrainVarietyDTO;
import com.aegis.wms.application.inventory.vo.GrainVarietyVO;

import java.util.List;

/**
 * 粮食品种服务接口
 */
public interface GrainVarietyService {

    /**
     * 创建粮食品种
     */
    Long create(GrainVarietyDTO dto);

    /**
     * 更新粮食品种
     */
    Boolean update(GrainVarietyDTO dto);

    /**
     * 删除粮食品种
     */
    Boolean delete(Long id);

    /**
     * 查询所有启用的粮食品种
     */
    List<GrainVarietyVO> listAll();

    /**
     * 根据ID查询
     */
    GrainVarietyVO getById(Long id);
}