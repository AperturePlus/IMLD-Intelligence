package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.Encounter;

import java.util.List;
import java.util.Optional;

/**
 * 就诊记录仓储接口，负责在租户边界内持久化患者就诊数据。
 */
public interface EncounterRepository {
    /**
     * 按租户和就诊记录主键查询就诊记录。
     *
     * @param tenantId 租户标识
     * @param id 就诊记录主键
     * @return 匹配的就诊记录，不存在时返回空
     */
    Optional<Encounter> findById(Long tenantId, Long id);

    /**
     * 按租户和就诊号查询就诊记录。
     *
     * @param tenantId 租户标识
     * @param encounterNo 就诊号
     * @return 匹配的就诊记录，不存在时返回空
     */
    Optional<Encounter> findByEncounterNo(Long tenantId, String encounterNo);

    /**
     * 查询租户下全部就诊记录。
     *
     * @param tenantId 租户标识
     * @return 符合条件的就诊记录列表
     */
    List<Encounter> listByTenantId(Long tenantId);

    /**
     * 按租户和患者主键查询就诊记录列表。
     *
     * @param tenantId 租户标识
     * @param patientId 患者主键
     * @return 符合条件的就诊记录列表
     */
    List<Encounter> listByPatientId(Long tenantId, Long patientId);

    /**
     * 新增就诊记录。
     *
     * @param encounter 待保存的就诊记录
     * @return 保存后的就诊记录
     */
    Encounter save(Encounter encounter);

    /**
     * 更新就诊记录。
     *
     * @param encounter 待更新的就诊记录
     * @return 更新后的就诊记录
     */
    Encounter update(Encounter encounter);

    /**
     * 按租户和就诊记录主键删除就诊记录。
     *
     * @param tenantId 租户标识
     * @param id 就诊记录主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
