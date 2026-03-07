package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;

import java.util.List;
import java.util.Optional;

/**
 * 知情同意记录仓储接口，负责在租户边界内持久化患者授权与同意记录。
 */
public interface ConsentRecordRepository {
    /**
     * 按租户和知情同意记录主键查询知情同意记录。
     *
     * @param tenantId 租户标识
     * @param id 知情同意记录主键
     * @return 匹配的知情同意记录，不存在时返回空
     */
    Optional<ConsentRecord> findById(Long tenantId, Long id);

    /**
     * 查询租户下全部知情同意记录。
     *
     * @param tenantId 租户标识
     * @return 符合条件的知情同意记录列表
     */
    List<ConsentRecord> listByTenantId(Long tenantId);

    /**
     * 按租户和患者主键查询知情同意记录列表。
     *
     * @param tenantId 租户标识
     * @param patientId 患者主键
     * @return 符合条件的知情同意记录列表
     */
    List<ConsentRecord> listByPatientId(Long tenantId, Long patientId);

    /**
     * 按租户和C端用户主键查询知情同意记录列表。
     *
     * @param tenantId 租户标识
     * @param tocUserId C端用户主键
     * @return 符合条件的知情同意记录列表
     */
    List<ConsentRecord> listByTocUserId(Long tenantId, Long tocUserId);

    /**
     * 新增知情同意记录。
     *
     * @param consentRecord 待保存的知情同意记录
     * @return 保存后的知情同意记录
     */
    ConsentRecord save(ConsentRecord consentRecord);

    /**
     * 更新知情同意记录。
     *
     * @param consentRecord 待更新的知情同意记录
     * @return 更新后的知情同意记录
     */
    ConsentRecord update(ConsentRecord consentRecord);

    /**
     * 按租户和知情同意记录主键删除知情同意记录。
     *
     * @param tenantId 租户标识
     * @param id 知情同意记录主键
     * @return 删除成功时返回 {@code true}，否则返回 {@code false}
     */
    Boolean deleteById(Long tenantId, Long id);
}
