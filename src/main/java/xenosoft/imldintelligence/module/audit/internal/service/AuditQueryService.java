package xenosoft.imldintelligence.module.audit.internal.service;

import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.ModelInvocationLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.SensitiveDataAccessLogQuery;
import xenosoft.imldintelligence.module.audit.internal.service.model.PageResult;
import xenosoft.imldintelligence.shared.model.AuditLog;
import xenosoft.imldintelligence.shared.model.ModelInvocationLog;
import xenosoft.imldintelligence.shared.model.SensitiveDataAccessLog;

public interface AuditQueryService {
    PageResult<AuditLog> queryAuditLogs(AuditLogQuery query, int page, int size);

    PageResult<SensitiveDataAccessLog> querySensitiveAccessLogs(SensitiveDataAccessLogQuery query, int page, int size);

    PageResult<ModelInvocationLog> queryModelInvocationLogs(ModelInvocationLogQuery query, int page, int size);
}
