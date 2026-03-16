package xenosoft.imldintelligence.module.audit.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.common.model.ModelInvocationLog;
import xenosoft.imldintelligence.module.audit.internal.repository.ModelInvocationLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.mybatis.ModelInvocationLogMapper;
import xenosoft.imldintelligence.module.audit.internal.repository.query.ModelInvocationLogQuery;

import java.util.List;

/**
 * 模型调用日志仓储实现类，基于 MyBatis-Plus 完成模型调用日志的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class ModelInvocationLogRepositoryImpl implements ModelInvocationLogRepository {
    private final ModelInvocationLogMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelInvocationLog save(ModelInvocationLog log) {
        mapper.insert(log);
        return log;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ModelInvocationLog> query(ModelInvocationLogQuery query, int offset, int limit) {
        return mapper.selectList(buildWrapper(query)
                .orderByDesc(ModelInvocationLog::getCreatedAt, ModelInvocationLog::getId)
                .last("LIMIT " + Math.max(1, limit) + " OFFSET " + Math.max(0, offset)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(ModelInvocationLogQuery query) {
        return mapper.selectCount(buildWrapper(query));
    }

    private LambdaQueryWrapper<ModelInvocationLog> buildWrapper(ModelInvocationLogQuery query) {
        return Wrappers.<ModelInvocationLog>lambdaQuery()
                .eq(ModelInvocationLog::getTenantId, query.getTenantId())
                .eq(query.getSessionId() != null, ModelInvocationLog::getSessionId, query.getSessionId())
                .eq(query.getModelRegistryId() != null, ModelInvocationLog::getModelRegistryId, query.getModelRegistryId())
                .eq(query.getProvider() != null && !query.getProvider().isEmpty(), ModelInvocationLog::getProvider, query.getProvider())
                .eq(query.getRequestId() != null && !query.getRequestId().isEmpty(), ModelInvocationLog::getRequestId, query.getRequestId())
                .eq(query.getStatus() != null && !query.getStatus().isEmpty(), ModelInvocationLog::getStatus, query.getStatus())
                .ge(query.getFrom() != null, ModelInvocationLog::getCreatedAt, query.getFrom())
                .le(query.getTo() != null, ModelInvocationLog::getCreatedAt, query.getTo());
    }
}
