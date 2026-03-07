package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;

import java.util.List;
import java.util.Optional;

/**
 * 患者外部标识仓储接口，负责在租户边界内维护患者外部身份映射。
 */
public interface PatientExternalIdRepository {
    /**
     * 按租户和患者外部标识主键查询患者外部标识。
     *
     * @param tenantId 租户标识
     * @param id 患者外部标识主键
     * @return 匹配的患者外部标识，不存在时返回空
     */
    Optional<PatientExternalId> findById(Long tenantId, Long id);

    /**
     * 按租户和外部标识类型和外部标识值查询患者外部标识。
     *
     * @param tenantId 租户标识
     * @param idType 外部标识类型
     * @param idValue 外部标识值
     * @return 匹配的患者外部标识，不存在时返回空
     */
    Optional<PatientExternalId> findByIdTypeAndIdValue(Long tenantId, String idType, String idValue);

    /**
     * 按租户和患者主键查询患者外部标识列表。
     *
     * @param tenantId 租户标识
     * @param patientId 患者主键
     * @return 符合条件的患者外部标识列表
     */
    List<PatientExternalId> listByPatientId(Long tenantId, Long patientId);

    /**
     * 查询租户下全部患者外部标识。
     *
     * @param tenantId 租户标识
     * @return 符合条件的患者外部标识列表
     */
    List<PatientExternalId> listByTenantId(Long tenantId);

    /**
     * 新增患者外部标识。
     *
     * @param patientExternalId 待保存的患者外部标识
     * @return 保存后的患者外部标识
     */
    PatientExternalId save(PatientExternalId patientExternalId);

    /**
     * 更新患者外部标识。
     *
     * @param patientExternalId 待更新的患者外部标识
     * @return 更新后的患者外部标识
     */
    PatientExternalId update(PatientExternalId patientExternalId);

    /**
     * 按租户和患者外部标识主键删除患者外部标识。
     *
     * @param tenantId 租户标识
     * @param id 患者外部标识主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
