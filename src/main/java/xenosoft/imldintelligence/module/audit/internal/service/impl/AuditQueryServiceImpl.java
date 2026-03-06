package xenosoft.imldintelligence.module.audit.internal.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.audit.internal.repository.AuditLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.ModelInvocationLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.SensitiveDataAccessLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.ModelInvocationLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.SensitiveDataAccessLogQuery;
import xenosoft.imldintelligence.module.audit.internal.service.AuditQueryService;
import xenosoft.imldintelligence.module.audit.internal.service.model.PageResult;
import xenosoft.imldintelligence.common.model.AuditLog;
import xenosoft.imldintelligence.common.model.ModelInvocationLog;
import xenosoft.imldintelligence.common.model.SensitiveDataAccessLog;

import java.util.Collections;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "imld.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditQueryServiceImpl implements AuditQueryService {
    private final AuditLogRepository auditLogRepository;
    private final SensitiveDataAccessLogRepository sensitiveDataAccessLogRepository;
    private final ModelInvocationLogRepository modelInvocationLogRepository;

    public AuditQueryServiceImpl(AuditLogRepository auditLogRepository,
                                 SensitiveDataAccessLogRepository sensitiveDataAccessLogRepository,
                                 ModelInvocationLogRepository modelInvocationLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.sensitiveDataAccessLogRepository = sensitiveDataAccessLogRepository;
        this.modelInvocationLogRepository = modelInvocationLogRepository;
    }

    @Override
    public PageResult<AuditLog> queryAuditLogs(AuditLogQuery query, int page, int size) {
        int offset = page * size;
        long total = auditLogRepository.count(query);
        if (total == 0 || offset >= total) {
            return new PageResult<>(page, size, total, Collections.emptyList());
        }
        List<AuditLog> items = auditLogRepository.query(query, offset, size);
        return new PageResult<>(page, size, total, items);
    }

    @Override
    public PageResult<SensitiveDataAccessLog> querySensitiveAccessLogs(SensitiveDataAccessLogQuery query, int page, int size) {
        int offset = page * size;
        long total = sensitiveDataAccessLogRepository.count(query);
        if (total == 0 || offset >= total) {
            return new PageResult<>(page, size, total, Collections.emptyList());
        }
        List<SensitiveDataAccessLog> items = sensitiveDataAccessLogRepository.query(query, offset, size);
        return new PageResult<>(page, size, total, items);
    }

    @Override
    public PageResult<ModelInvocationLog> queryModelInvocationLogs(ModelInvocationLogQuery query, int page, int size) {
        int offset = page * size;
        long total = modelInvocationLogRepository.count(query);
        if (total == 0 || offset >= total) {
            return new PageResult<>(page, size, total, Collections.emptyList());
        }
        List<ModelInvocationLog> items = modelInvocationLogRepository.query(query, offset, size);
        return new PageResult<>(page, size, total, items);
    }
}
