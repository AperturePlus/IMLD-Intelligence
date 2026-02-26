package xenosoft.imldintelligence.module.audit.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;
import xenosoft.imldintelligence.shared.model.AuditLog;

import java.util.List;

@Mapper
public interface AuditLogMapper {
    int insert(AuditLog auditLog);

    List<AuditLog> query(@Param("query") AuditLogQuery query,
                         @Param("offset") int offset,
                         @Param("limit") int limit);

    long count(@Param("query") AuditLogQuery query);
}
