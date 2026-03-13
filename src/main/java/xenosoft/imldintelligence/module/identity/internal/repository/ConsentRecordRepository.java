package xenosoft.imldintelligence.module.identity.internal.repository;

import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;

import java.util.List;
import java.util.Optional;

/**
 * 知情同意记录仓储接口，负责在租户边界内持久化患者授权与同意记录。
 */
public interface ConsentRecordRepository {
    Optional<ConsentRecord> findById(Long tenantId, Long id);
    List<ConsentRecord> listByTenantId(Long tenantId);
    List<ConsentRecord> listByPatientId(Long tenantId, Long patientId);
    List<ConsentRecord> listByTocUserId(Long tenantId, Long tocUserId);

    long countByCondition(Long tenantId, Long patientId, String consentType, String status);

    List<ConsentRecord> listByCondition(Long tenantId, Long patientId, String consentType,
                                         String status, long offset, int limit);

    ConsentRecord save(ConsentRecord consentRecord);
    ConsentRecord update(ConsentRecord consentRecord);
    Boolean deleteById(Long tenantId, Long id);
}
