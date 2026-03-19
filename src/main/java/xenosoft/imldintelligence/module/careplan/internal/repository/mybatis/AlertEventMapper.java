package xenosoft.imldintelligence.module.careplan.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.careplan.internal.model.AlertEvent;

/**
 * 预警事件 MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface AlertEventMapper extends BaseMapper<AlertEvent> {
}
