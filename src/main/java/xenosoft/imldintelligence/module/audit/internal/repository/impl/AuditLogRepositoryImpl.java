package xenosoft.imldintelligence.module.audit.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.audit.internal.repository.AuditLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.mybatis.AuditLogMapper;
import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;
import xenosoft.imldintelligence.shared.model.AuditLog;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final AuditLogMapper mapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        mapper.insert(auditLog);
        return auditLog;
    }

    @Override
    public List<AuditLog> query(AuditLogQuery query, int offset, int limit) {
        return mapper.query(query, offset, limit);
    }

    @Override
    public long count(AuditLogQuery query) {
        return mapper.count(query);
    }
}
