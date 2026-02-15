package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;

import java.util.List;

@Mapper
public interface TenantMapper {
    Tenant findById(@Param("id") Long id);

    Tenant findByTenantCode(@Param("tenantCode") String tenantCode);

    List<Tenant> listAll();

    int insert(Tenant tenant);

    int update(Tenant tenant);

    int deleteById(@Param("id") Long id);
}
