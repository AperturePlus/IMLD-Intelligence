package xenosoft.imldintelligence.module.audit.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.common.model.AuditLog;

/**
 * 审计日志 MyBatis-Plus Mapper，复用 BaseMapper 减少重复 CRUD SQL。
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
