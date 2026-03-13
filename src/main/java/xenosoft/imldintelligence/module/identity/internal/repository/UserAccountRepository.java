package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;

import java.util.List;
import java.util.Optional;

/**
 * 用户账号仓储接口，负责在租户边界内维护后台用户账号数据。
 */
public interface UserAccountRepository {
    /**
     * 按租户和用户账号主键查询用户账号。
     *
     * @param tenantId 租户标识
     * @param id 用户账号主键
     * @return 匹配的用户账号，不存在时返回空
     */
    Optional<UserAccount> findById(Long tenantId, Long id);

    /**
     * 按租户和用户编号查询用户账号。
     *
     * @param tenantId 租户标识
     * @param userNo 用户编号
     * @return 匹配的用户账号，不存在时返回空
     */
    Optional<UserAccount> findByUserNo(Long tenantId, String userNo);

    /**
     * 按租户和登录名查询用户账号。
     *
     * @param tenantId 租户标识
     * @param username 登录名
     * @return 匹配的用户账号，不存在时返回空
     */
    Optional<UserAccount> findByUsername(Long tenantId, String username);

    /**
     * 查询租户下全部用户账号。
     *
     * @param tenantId 租户标识
     * @return 符合条件的用户账号列表
     */
    List<UserAccount> listByTenantId(Long tenantId);

    long countByCondition(Long tenantId, String usernameKeyword, String userType,
                          String deptName, String status);

    List<UserAccount> listByCondition(Long tenantId, String usernameKeyword, String userType,
                                       String deptName, String status, long offset, int limit);

    /**
     * 新增用户账号。
     *
     * @param userAccount 待保存的用户账号
     * @return 保存后的用户账号
     */
    UserAccount save(UserAccount userAccount);

    /**
     * 更新用户账号。
     *
     * @param userAccount 待更新的用户账号
     * @return 更新后的用户账号
     */
    UserAccount update(UserAccount userAccount);

    /**
     * 按租户和用户账号主键删除用户账号。
     *
     * @param tenantId 租户标识
     * @param id 用户账号主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
