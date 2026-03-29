package com.aegis.wms.domain.graincondition.repository;

import com.aegis.wms.domain.graincondition.entity.GrainConditionAttachment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 粮情附件仓储接口
 */
@Mapper
public interface GrainConditionAttachmentRepository extends BaseMapper<GrainConditionAttachment> {
}