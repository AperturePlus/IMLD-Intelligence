package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;

import java.util.List;

@Mapper
public interface TocUserMapper {
    TocUser findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    TocUser findByTocUid(@Param("tenantId") Long tenantId, @Param("tocUid") String tocUid);

    List<TocUser> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(TocUser tocUser);

    int update(TocUser tocUser);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
