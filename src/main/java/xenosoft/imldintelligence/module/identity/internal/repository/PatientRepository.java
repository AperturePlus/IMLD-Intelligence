package xenosoft.imldintelligence.module.identity.internal.repository;


import xenosoft.imldintelligence.module.identity.internal.model.Patient;

import java.util.List;
import java.util.Optional;

/**
 * 患者仓储接口，负责在租户边界内持久化患者主数据。
 */
public interface PatientRepository {
    /**
     * 按租户和患者主键查询患者。
     *
     * @param tenantId 租户标识
     * @param patientId 患者主键
     * @return 匹配的患者，不存在时返回空
     */
    Optional<Patient> findById(Long tenantId, Long patientId);
    /**
     * 按租户和患者编号查询患者。
     *
     * @param tenantId 租户标识
     * @param patientNo 患者编号
     * @return 匹配的患者，不存在时返回空
     */
    Optional<Patient> findByPatientNo(Long tenantId, String patientNo);
    /**
     * 查询租户下全部患者。
     *
     * @param tenantId 租户标识
     * @return 符合条件的患者列表
     */
    List<Patient> listByTenantId(Long tenantId);
    /**
     * 新增患者。
     *
     * @param patient 待保存的患者
     * @return 保存后的患者
     */
    Patient save(Patient patient);
    /**
     * 更新患者。
     *
     * @param patient 待更新的患者
     * @return 更新后的患者
     */
    Patient update(Patient patient);
    /**
     * 按租户和患者主键删除患者。
     *
     * @param tenantId 租户标识
     * @param patientId 患者主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long patientId);
    /**
     * 按租户和患者编号删除患者。
     *
     * @param tenantId 租户标识
     * @param patientNo 患者编号
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteByPatientNo(Long tenantId, String patientNo);

}
