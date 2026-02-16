package xenosoft.imldintelligence.module.diagnoses.internal.repository;

import xenosoft.imldintelligence.module.diagnoses.model.DiagnosisResult;

import java.util.List;
import java.util.Optional;

public interface DiagnosisResultRepository {
    Optional<DiagnosisResult> findById(Long tenantId, Long id);

    List<DiagnosisResult> listByTenantId(Long tenantId);

    List<DiagnosisResult> listBySessionId(Long tenantId, Long sessionId);

    DiagnosisResult save(DiagnosisResult diagnosisResult);

    DiagnosisResult update(DiagnosisResult diagnosisResult);

    Boolean deleteById(Long tenantId, Long id);
}

