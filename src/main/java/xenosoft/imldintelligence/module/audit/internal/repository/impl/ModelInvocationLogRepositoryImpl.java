package xenosoft.imldintelligence.module.audit.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.audit.internal.repository.ModelInvocationLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.mybatis.ModelInvocationLogMapper;
import xenosoft.imldintelligence.module.audit.internal.repository.query.ModelInvocationLogQuery;
import xenosoft.imldintelligence.common.model.ModelInvocationLog;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ModelInvocationLogRepositoryImpl implements ModelInvocationLogRepository {
    private final ModelInvocationLogMapper mapper;

    @Override
    public ModelInvocationLog save(ModelInvocationLog log) {
        mapper.insert(log);
        return log;
    }

    @Override
    public List<ModelInvocationLog> query(ModelInvocationLogQuery query, int offset, int limit) {
        return mapper.query(query, offset, limit);
    }

    @Override
    public long count(ModelInvocationLogQuery query) {
        return mapper.count(query);
    }
}
