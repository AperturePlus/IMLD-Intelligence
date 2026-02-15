package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;

import java.util.List;

@Mapper
public interface UserRoleRelMapper {
    UserRoleRel findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    UserRoleRel findByUserIdAndRoleId(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("roleId") Long roleId);

    List<UserRoleRel> listByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    List<UserRoleRel> listByRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    int insert(UserRoleRel userRoleRel);

    int update(UserRoleRel userRoleRel);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    int deleteByUserIdAndRoleId(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("roleId") Long roleId);
}
