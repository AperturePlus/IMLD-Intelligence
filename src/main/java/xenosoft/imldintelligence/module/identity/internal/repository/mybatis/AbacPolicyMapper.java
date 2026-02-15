package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;

import java.util.List;

@Mapper
public interface AbacPolicyMapper {
    AbacPolicy findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    AbacPolicy findByPolicyCode(@Param("tenantId") Long tenantId, @Param("policyCode") String policyCode);

    List<AbacPolicy> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(AbacPolicy abacPolicy);

    int update(AbacPolicy abacPolicy);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
