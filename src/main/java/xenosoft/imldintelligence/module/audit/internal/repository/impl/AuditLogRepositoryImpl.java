package xenosoft.imldintelligence.module.audit.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.common.model.AuditLog;
import xenosoft.imldintelligence.module.audit.internal.repository.AuditLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.mybatis.AuditLogMapper;
import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;

import java.util.List;

/**
 * 审计日志仓储实现类，基于 MyBatis-Plus 完成审计日志的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final AuditLogMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditLog save(AuditLog auditLog) {
        mapper.insert(auditLog);
        return auditLog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuditLog> query(AuditLogQuery query, int offset, int limit) {
        return mapper.selectList(buildWrapper(query)
                .orderByDesc(AuditLog::getCreatedAt, AuditLog::getId)
                .last("LIMIT " + Math.max(1, limit) + " OFFSET " + Math.max(0, offset)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(AuditLogQuery query) {
        return mapper.selectCount(buildWrapper(query));
    }

    private LambdaQueryWrapper<AuditLog> buildWrapper(AuditLogQuery query) {
        return Wrappers.<AuditLog>lambdaQuery()
                .eq(AuditLog::getTenantId, query.getTenantId())
                .eq(query.getUserId() != null, AuditLog::getUserId, query.getUserId())
                .eq(query.getAction() != null && !query.getAction().isEmpty(), AuditLog::getAction, query.getAction())
                .eq(query.getResourceType() != null && !query.getResourceType().isEmpty(), AuditLog::getResourceType, query.getResourceType())
                .eq(query.getResourceId() != null && !query.getResourceId().isEmpty(), AuditLog::getResourceId, query.getResourceId())
                .eq(query.getTraceId() != null && !query.getTraceId().isEmpty(), AuditLog::getTraceId, query.getTraceId())
                .ge(query.getFrom() != null, AuditLog::getCreatedAt, query.getFrom())
                .le(query.getTo() != null, AuditLog::getCreatedAt, query.getTo());
    }
}
