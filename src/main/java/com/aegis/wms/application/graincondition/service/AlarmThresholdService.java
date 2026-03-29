package com.aegis.wms.application.graincondition.service;

import com.aegis.wms.application.graincondition.dto.AlarmThresholdDTO;
import com.aegis.wms.application.graincondition.vo.AlarmThresholdVO;

import java.util.List;

/**
 * 报警阈值服务接口
 */
public interface AlarmThresholdService {

    /**
     * 创建报警阈值
     *
     * @param dto 阈值DTO
     * @return 阈值ID
     */
    Long create(AlarmThresholdDTO dto);

    /**
     * 更新报警阈值
     *
     * @param dto 阈值DTO
     * @return 是否成功
     */
    Boolean update(AlarmThresholdDTO dto);

    /**
     * 删除报警阈值
     *
     * @param id 阈值ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 根据ID查询阈值
     *
     * @param id 阈值ID
     * @return 阈值VO
     */
    AlarmThresholdVO getById(Long id);

    /**
     * 查询所有启用的阈值
     *
     * @return 阈值列表
     */
    List<AlarmThresholdVO> listAllEnabled();
}