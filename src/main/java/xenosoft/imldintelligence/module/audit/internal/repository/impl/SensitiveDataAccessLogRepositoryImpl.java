package xenosoft.imldintelligence.module.audit.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.audit.internal.repository.SensitiveDataAccessLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.mybatis.SensitiveDataAccessLogMapper;
import xenosoft.imldintelligence.module.audit.internal.repository.query.SensitiveDataAccessLogQuery;
import xenosoft.imldintelligence.shared.model.SensitiveDataAccessLog;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SensitiveDataAccessLogRepositoryImpl implements SensitiveDataAccessLogRepository {
    private final SensitiveDataAccessLogMapper mapper;

    @Override
    public SensitiveDataAccessLog save(SensitiveDataAccessLog log) {
        mapper.insert(log);
        return log;
    }

    @Override
    public List<SensitiveDataAccessLog> query(SensitiveDataAccessLogQuery query, int offset, int limit) {
        return mapper.query(query, offset, limit);
    }

    @Override
    public long count(SensitiveDataAccessLogQuery query) {
        return mapper.count(query);
    }
}
