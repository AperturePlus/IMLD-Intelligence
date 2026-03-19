package xenosoft.imldintelligence.module.audit.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.common.model.SensitiveDataAccessLog;

/**
 * 敏感数据访问日志 MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface SensitiveDataAccessLogMapper extends BaseMapper<SensitiveDataAccessLog> {
}
