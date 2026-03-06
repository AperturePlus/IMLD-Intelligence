package xenosoft.imldintelligence.module.audit.internal.repository;

import xenosoft.imldintelligence.module.audit.internal.repository.query.ModelInvocationLogQuery;
import xenosoft.imldintelligence.common.model.ModelInvocationLog;

import java.util.List;

public interface ModelInvocationLogRepository {
    ModelInvocationLog save(ModelInvocationLog log);

    List<ModelInvocationLog> query(ModelInvocationLogQuery query, int offset, int limit);

    long count(ModelInvocationLogQuery query);
}
