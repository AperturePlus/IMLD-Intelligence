package xenosoft.imldintelligence.module.identity.internal.repository;


import xenosoft.imldintelligence.module.identity.internal.model.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientRepository {
    Optional<Patient> findById(Long tenantId, Long patientId);
    Optional<Patient> findByPatientNo(Long tenantId, String patientNo);
    List<Patient> listByTenantId(Long tenantId);
    Patient save(Patient patient);
    Patient update(Patient patient);
    Boolean deleteById(Long tenantId, Long patientId);
    Boolean deleteByPatientNo(Long tenantId, String patientNo);

}
