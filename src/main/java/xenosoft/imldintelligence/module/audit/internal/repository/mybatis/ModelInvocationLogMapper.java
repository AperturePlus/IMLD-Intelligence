package xenosoft.imldintelligence.module.audit.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.audit.internal.repository.query.ModelInvocationLogQuery;
import xenosoft.imldintelligence.shared.model.ModelInvocationLog;

import java.util.List;

@Mapper
public interface ModelInvocationLogMapper {
    int insert(ModelInvocationLog log);

    List<ModelInvocationLog> query(@Param("query") ModelInvocationLogQuery query,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    long count(@Param("query") ModelInvocationLogQuery query);
}
