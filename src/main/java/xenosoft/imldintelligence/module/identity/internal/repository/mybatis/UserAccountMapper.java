package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;

import java.util.List;

@Mapper
public interface UserAccountMapper {
    UserAccount findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    UserAccount findByUserNo(@Param("tenantId") Long tenantId, @Param("userNo") String userNo);

    UserAccount findByUsername(@Param("tenantId") Long tenantId, @Param("username") String username);

    List<UserAccount> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(UserAccount userAccount);

    int update(UserAccount userAccount);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
