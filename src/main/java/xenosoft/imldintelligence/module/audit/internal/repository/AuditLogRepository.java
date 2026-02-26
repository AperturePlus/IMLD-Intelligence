package xenosoft.imldintelligence.module.audit.internal.repository;

import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;
import xenosoft.imldintelligence.shared.model.AuditLog;

import java.util.List;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);

    List<AuditLog> query(AuditLogQuery query, int offset, int limit);

    long count(AuditLogQuery query);
}
