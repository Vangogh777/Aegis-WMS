package com.aegis.wms.domain.graincondition.repository;

import com.aegis.wms.domain.graincondition.entity.AlarmRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报警记录仓储接口
 */
@Mapper
public interface AlarmRecordRepository extends BaseMapper<AlarmRecord> {
}