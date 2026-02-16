package xenosoft.imldintelligence.module.diagnoses.internal.repository;

import xenosoft.imldintelligence.module.diagnoses.model.DiagnosisSession;

import java.util.List;
import java.util.Optional;

public interface DiagnosisSessionRepository {
    Optional<DiagnosisSession> findById(Long tenantId, Long id);

    List<DiagnosisSession> listByTenantId(Long tenantId);

    List<DiagnosisSession> listByPatientId(Long tenantId, Long patientId);

    List<DiagnosisSession> listByEncounterId(Long tenantId, Long encounterId);

    DiagnosisSession save(DiagnosisSession diagnosisSession);

    DiagnosisSession update(DiagnosisSession diagnosisSession);

    Boolean deleteById(Long tenantId, Long id);
}

