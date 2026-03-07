package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.Tenant;

import java.util.List;
import java.util.Optional;

/**
 * 租户仓储接口，负责维护平台租户基础信息。
 */
public interface TenantRepository {
    /**
     * 按租户主键查询租户。
     *
     * @param id 租户主键
     * @return 匹配的租户，不存在时返回空
     */
    Optional<Tenant> findById(Long id);

    /**
     * 按租户编码查询租户。
     *
     * @param tenantCode 租户编码
     * @return 匹配的租户，不存在时返回空
     */
    Optional<Tenant> findByTenantCode(String tenantCode);

    /**
     * 查询全部租户。
     *
     * @return 全部租户列表
     */
    List<Tenant> listAll();

    /**
     * 新增租户。
     *
     * @param tenant 待保存的租户
     * @return 保存后的租户
     */
    Tenant save(Tenant tenant);

    /**
     * 更新租户。
     *
     * @param tenant 待更新的租户
     * @return 更新后的租户
     */
    Tenant update(Tenant tenant);

    /**
     * 按租户主键删除租户。
     *
     * @param id 租户主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long id);
}
