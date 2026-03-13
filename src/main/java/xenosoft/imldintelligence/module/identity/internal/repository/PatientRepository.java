package xenosoft.imldintelligence.module.identity.internal.repository;


import xenosoft.imldintelligence.module.identity.internal.model.Patient;

import java.util.List;
import java.util.Optional;

/**
 * 患者仓储接口，负责在租户边界内持久化患者主数据。
 */
public interface PatientRepository {
    Optional<Patient> findById(Long tenantId, Long patientId);
    Optional<Patient> findByPatientNo(Long tenantId, String patientNo);
    List<Patient> listByTenantId(Long tenantId);

    long countByCondition(Long tenantId, String patientNo, String patientNameKeyword,
                          String patientType, String status);

    List<Patient> listByCondition(Long tenantId, String patientNo, String patientNameKeyword,
                                   String patientType, String status, long offset, int limit);

    Patient save(Patient patient);
    Patient update(Patient patient);
    Boolean deleteById(Long tenantId, Long patientId);
    Boolean deleteByPatientNo(Long tenantId, String patientNo);
}
