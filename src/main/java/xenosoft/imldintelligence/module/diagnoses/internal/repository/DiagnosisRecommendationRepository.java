package xenosoft.imldintelligence.module.diagnoses.internal.repository;

import xenosoft.imldintelligence.module.diagnoses.model.DiagnosisRecommendation;

import java.util.List;
import java.util.Optional;

public interface DiagnosisRecommendationRepository {
    Optional<DiagnosisRecommendation> findById(Long tenantId, Long id);

    List<DiagnosisRecommendation> listByTenantId(Long tenantId);

    List<DiagnosisRecommendation> listBySessionId(Long tenantId, Long sessionId);

    DiagnosisRecommendation save(DiagnosisRecommendation diagnosisRecommendation);

    DiagnosisRecommendation update(DiagnosisRecommendation diagnosisRecommendation);

    Boolean deleteById(Long tenantId, Long id);
}

