package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;

import java.util.List;

/**
 * 用户账号 MyBatis Mapper，定义用户账号的数据读写映射。
 */
@Mapper
public interface UserAccountMapper {
    UserAccount findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    UserAccount findByUserNo(@Param("tenantId") Long tenantId, @Param("userNo") String userNo);

    UserAccount findByUsername(@Param("tenantId") Long tenantId, @Param("username") String username);

    List<UserAccount> listByTenantId(@Param("tenantId") Long tenantId);

    long countByCondition(@Param("tenantId") Long tenantId,
                          @Param("usernameKeyword") String usernameKeyword,
                          @Param("userType") String userType,
                          @Param("deptName") String deptName,
                          @Param("status") String status);

    List<UserAccount> listByCondition(@Param("tenantId") Long tenantId,
                                      @Param("usernameKeyword") String usernameKeyword,
                                      @Param("userType") String userType,
                                      @Param("deptName") String deptName,
                                      @Param("status") String status,
                                      @Param("offset") long offset,
                                      @Param("limit") int limit);

    int insert(UserAccount userAccount);

    int update(UserAccount userAccount);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
