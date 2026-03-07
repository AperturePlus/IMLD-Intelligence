package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.Role;

import java.util.List;

/**
 * 角色 MyBatis Mapper，定义角色的数据读写映射。
 */
@Mapper
public interface RoleMapper {
    Role findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    Role findByRoleCode(@Param("tenantId") Long tenantId, @Param("roleCode") String roleCode);

    List<Role> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(Role role);

    int update(Role role);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
