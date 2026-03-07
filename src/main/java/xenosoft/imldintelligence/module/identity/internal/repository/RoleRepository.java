package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.Role;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储接口，负责在租户边界内维护角色主数据。
 */
public interface RoleRepository {
    /**
     * 按租户和角色主键查询角色。
     *
     * @param tenantId 租户标识
     * @param id 角色主键
     * @return 匹配的角色，不存在时返回空
     */
    Optional<Role> findById(Long tenantId, Long id);

    /**
     * 按租户和角色编码查询角色。
     *
     * @param tenantId 租户标识
     * @param roleCode 角色编码
     * @return 匹配的角色，不存在时返回空
     */
    Optional<Role> findByRoleCode(Long tenantId, String roleCode);

    /**
     * 查询租户下全部角色。
     *
     * @param tenantId 租户标识
     * @return 符合条件的角色列表
     */
    List<Role> listByTenantId(Long tenantId);

    /**
     * 新增角色。
     *
     * @param role 待保存的角色
     * @return 保存后的角色
     */
    Role save(Role role);

    /**
     * 更新角色。
     *
     * @param role 待更新的角色
     * @return 更新后的角色
     */
    Role update(Role role);

    /**
     * 按租户和角色主键删除角色。
     *
     * @param tenantId 租户标识
     * @param id 角色主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
