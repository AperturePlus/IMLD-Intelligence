package xenosoft.imldintelligence.module.audit.internal.repository;

import xenosoft.imldintelligence.module.audit.internal.repository.query.SensitiveDataAccessLogQuery;
import xenosoft.imldintelligence.shared.model.SensitiveDataAccessLog;

import java.util.List;

public interface SensitiveDataAccessLogRepository {
    SensitiveDataAccessLog save(SensitiveDataAccessLog log);

    List<SensitiveDataAccessLog> query(SensitiveDataAccessLogQuery query, int offset, int limit);

    long count(SensitiveDataAccessLogQuery query);
}
