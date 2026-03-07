package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;

import java.util.List;
import java.util.Optional;

/**
 * 监护人关系仓储接口，负责在租户边界内维护患者监护关系数据。
 */
public interface GuardianRelationRepository {
    /**
     * 按租户和监护人关系主键查询监护人关系。
     *
     * @param tenantId 租户标识
     * @param id 监护人关系主键
     * @return 匹配的监护人关系，不存在时返回空
     */
    Optional<GuardianRelation> findById(Long tenantId, Long id);

    /**
     * 按租户和患者主键查询主监护人关系。
     *
     * @param tenantId 租户标识
     * @param patientId 患者主键
     * @return 匹配的监护人关系，不存在时返回空
     */
    Optional<GuardianRelation> findPrimaryByPatientId(Long tenantId, Long patientId);

    /**
     * 按租户和患者主键查询监护人关系列表。
     *
     * @param tenantId 租户标识
     * @param patientId 患者主键
     * @return 符合条件的监护人关系列表
     */
    List<GuardianRelation> listByPatientId(Long tenantId, Long patientId);

    /**
     * 查询租户下全部监护人关系。
     *
     * @param tenantId 租户标识
     * @return 符合条件的监护人关系列表
     */
    List<GuardianRelation> listByTenantId(Long tenantId);

    /**
     * 新增监护人关系。
     *
     * @param guardianRelation 待保存的监护人关系
     * @return 保存后的监护人关系
     */
    GuardianRelation save(GuardianRelation guardianRelation);

    /**
     * 更新监护人关系。
     *
     * @param guardianRelation 待更新的监护人关系
     * @return 更新后的监护人关系
     */
    GuardianRelation update(GuardianRelation guardianRelation);

    /**
     * 按租户和监护人关系主键删除监护人关系。
     *
     * @param tenantId 租户标识
     * @param id 监护人关系主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
