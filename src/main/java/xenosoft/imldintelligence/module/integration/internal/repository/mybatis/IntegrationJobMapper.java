package xenosoft.imldintelligence.module.integration.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.integration.internal.model.IntegrationJob;

/**
 * 集成任务 MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface IntegrationJobMapper extends BaseMapper<IntegrationJob> {
}
