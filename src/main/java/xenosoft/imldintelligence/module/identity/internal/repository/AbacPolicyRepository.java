package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;

import java.util.List;
import java.util.Optional;

/**
 * ABAC策略仓储接口，负责在租户边界内持久化属性访问控制策略数据。
 */
public interface AbacPolicyRepository {
    /**
     * 按租户和ABAC策略主键查询ABAC策略。
     *
     * @param tenantId 租户标识
     * @param id ABAC策略主键
     * @return 匹配的ABAC策略，不存在时返回空
     */
    Optional<AbacPolicy> findById(Long tenantId, Long id);

    /**
     * 按租户和策略编码查询ABAC策略。
     *
     * @param tenantId 租户标识
     * @param policyCode 策略编码
     * @return 匹配的ABAC策略，不存在时返回空
     */
    Optional<AbacPolicy> findByPolicyCode(Long tenantId, String policyCode);

    /**
     * 查询租户下全部ABAC策略。
     *
     * @param tenantId 租户标识
     * @return 符合条件的ABAC策略列表
     */
    List<AbacPolicy> listByTenantId(Long tenantId);

    /**
     * 新增ABAC策略。
     *
     * @param abacPolicy 待保存的ABAC策略
     * @return 保存后的ABAC策略
     */
    AbacPolicy save(AbacPolicy abacPolicy);

    /**
     * 更新ABAC策略。
     *
     * @param abacPolicy 待更新的ABAC策略
     * @return 更新后的ABAC策略
     */
    AbacPolicy update(AbacPolicy abacPolicy);

    /**
     * 按租户和ABAC策略主键删除ABAC策略。
     *
     * @param tenantId 租户标识
     * @param id ABAC策略主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
