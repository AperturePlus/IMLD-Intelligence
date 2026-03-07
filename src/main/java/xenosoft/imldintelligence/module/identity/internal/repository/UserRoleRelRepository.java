package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;

import java.util.List;
import java.util.Optional;

/**
 * 用户角色关系仓储接口，负责在租户边界内维护用户与角色的绑定关系。
 */
public interface UserRoleRelRepository {
    /**
     * 按租户和用户角色关系主键查询用户角色关系。
     *
     * @param tenantId 租户标识
     * @param id 用户角色关系主键
     * @return 匹配的用户角色关系，不存在时返回空
     */
    Optional<UserRoleRel> findById(Long tenantId, Long id);

    /**
     * 按租户和用户主键和角色主键查询用户角色关系。
     *
     * @param tenantId 租户标识
     * @param userId 用户主键
     * @param roleId 角色主键
     * @return 匹配的用户角色关系，不存在时返回空
     */
    Optional<UserRoleRel> findByUserIdAndRoleId(Long tenantId, Long userId, Long roleId);

    /**
     * 按租户和用户主键查询用户角色关系列表。
     *
     * @param tenantId 租户标识
     * @param userId 用户主键
     * @return 符合条件的用户角色关系列表
     */
    List<UserRoleRel> listByUserId(Long tenantId, Long userId);

    /**
     * 按租户和角色主键查询用户角色关系列表。
     *
     * @param tenantId 租户标识
     * @param roleId 角色主键
     * @return 符合条件的用户角色关系列表
     */
    List<UserRoleRel> listByRoleId(Long tenantId, Long roleId);

    /**
     * 新增用户角色关系。
     *
     * @param userRoleRel 待保存的用户角色关系
     * @return 保存后的用户角色关系
     */
    UserRoleRel save(UserRoleRel userRoleRel);

    /**
     * 更新用户角色关系。
     *
     * @param userRoleRel 待更新的用户角色关系
     * @return 更新后的用户角色关系
     */
    UserRoleRel update(UserRoleRel userRoleRel);

    /**
     * 按租户和用户角色关系主键删除用户角色关系。
     *
     * @param tenantId 租户标识
     * @param id 用户角色关系主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);

    /**
     * 按租户和用户主键和角色主键删除用户角色关系。
     *
     * @param tenantId 租户标识
     * @param userId 用户主键
     * @param roleId 角色主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteByUserIdAndRoleId(Long tenantId, Long userId, Long roleId);
}
