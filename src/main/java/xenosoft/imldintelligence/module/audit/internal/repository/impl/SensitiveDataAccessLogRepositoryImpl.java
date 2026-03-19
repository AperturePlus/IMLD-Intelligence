package xenosoft.imldintelligence.module.audit.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.common.model.SensitiveDataAccessLog;
import xenosoft.imldintelligence.module.audit.internal.repository.SensitiveDataAccessLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.mybatis.SensitiveDataAccessLogMapper;
import xenosoft.imldintelligence.module.audit.internal.repository.query.SensitiveDataAccessLogQuery;

import java.util.List;

/**
 * 敏感数据访问日志仓储实现类，基于 MyBatis-Plus 完成敏感数据访问日志的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class SensitiveDataAccessLogRepositoryImpl implements SensitiveDataAccessLogRepository {
    private final SensitiveDataAccessLogMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public SensitiveDataAccessLog save(SensitiveDataAccessLog log) {
        mapper.insert(log);
        return log;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SensitiveDataAccessLog> query(SensitiveDataAccessLogQuery query, int offset, int limit) {
        return mapper.selectList(buildWrapper(query)
                .orderByDesc(SensitiveDataAccessLog::getCreatedAt, SensitiveDataAccessLog::getId)
                .last("LIMIT " + Math.max(1, limit) + " OFFSET " + Math.max(0, offset)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(SensitiveDataAccessLogQuery query) {
        return mapper.selectCount(buildWrapper(query));
    }

    private LambdaQueryWrapper<SensitiveDataAccessLog> buildWrapper(SensitiveDataAccessLogQuery query) {
        return Wrappers.<SensitiveDataAccessLog>lambdaQuery()
                .eq(SensitiveDataAccessLog::getTenantId, query.getTenantId())
                .eq(query.getUserId() != null, SensitiveDataAccessLog::getUserId, query.getUserId())
                .eq(query.getSensitiveType() != null && !query.getSensitiveType().isEmpty(), SensitiveDataAccessLog::getSensitiveType, query.getSensitiveType())
                .eq(query.getResourceType() != null && !query.getResourceType().isEmpty(), SensitiveDataAccessLog::getResourceType, query.getResourceType())
                .eq(query.getResourceId() != null && !query.getResourceId().isEmpty(), SensitiveDataAccessLog::getResourceId, query.getResourceId())
                .eq(query.getAccessResult() != null && !query.getAccessResult().isEmpty(), SensitiveDataAccessLog::getAccessResult, query.getAccessResult())
                .ge(query.getFrom() != null, SensitiveDataAccessLog::getCreatedAt, query.getFrom())
                .le(query.getTo() != null, SensitiveDataAccessLog::getCreatedAt, query.getTo());
    }
}
