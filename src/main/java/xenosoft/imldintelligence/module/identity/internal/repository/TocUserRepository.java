package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.TocUser;

import java.util.List;
import java.util.Optional;

/**
 * C端用户仓储接口，负责在租户边界内持久化C端用户数据。
 */
public interface TocUserRepository {
    /**
     * 按租户和C端用户主键查询C端用户。
     *
     * @param tenantId 租户标识
     * @param id C端用户主键
     * @return 匹配的C端用户，不存在时返回空
     */
    Optional<TocUser> findById(Long tenantId, Long id);

    /**
     * 按租户和C端用户唯一标识查询C端用户。
     *
     * @param tenantId 租户标识
     * @param tocUid C端用户唯一标识
     * @return 匹配的C端用户，不存在时返回空
     */
    Optional<TocUser> findByTocUid(Long tenantId, String tocUid);

    /**
     * 查询租户下全部C端用户。
     *
     * @param tenantId 租户标识
     * @return 符合条件的C端用户列表
     */
    List<TocUser> listByTenantId(Long tenantId);

    /**
     * 新增C端用户。
     *
     * @param tocUser 待保存的C端用户
     * @return 保存后的C端用户
     */
    TocUser save(TocUser tocUser);

    /**
     * 更新C端用户。
     *
     * @param tocUser 待更新的C端用户
     * @return 更新后的C端用户
     */
    TocUser update(TocUser tocUser);

    /**
     * 按租户和C端用户主键删除C端用户。
     *
     * @param tenantId 租户标识
     * @param id C端用户主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
