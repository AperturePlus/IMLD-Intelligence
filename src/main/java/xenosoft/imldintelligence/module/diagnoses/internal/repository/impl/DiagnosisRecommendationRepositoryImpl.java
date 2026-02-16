package xenosoft.imldintelligence.module.diagnoses.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisRecommendationRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis.DiagnosisRecommendationMapper;
import xenosoft.imldintelligence.module.diagnoses.model.DiagnosisRecommendation;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DiagnosisRecommendationRepositoryImpl implements DiagnosisRecommendationRepository {
    private final DiagnosisRecommendationMapper diagnosisRecommendationMapper;

    @Override
    public Optional<DiagnosisRecommendation> findById(Long tenantId, Long id) {
        return Optional.ofNullable(diagnosisRecommendationMapper.findById(tenantId, id));
    }

    @Override
    public List<DiagnosisRecommendation> listByTenantId(Long tenantId) {
        return diagnosisRecommendationMapper.listByTenantId(tenantId);
    }

    @Override
    public List<DiagnosisRecommendation> listBySessionId(Long tenantId, Long sessionId) {
        return diagnosisRecommendationMapper.listBySessionId(tenantId, sessionId);
    }

    @Override
    public DiagnosisRecommendation save(DiagnosisRecommendation diagnosisRecommendation) {
        diagnosisRecommendationMapper.insert(diagnosisRecommendation);
        return diagnosisRecommendation;
    }

    @Override
    public DiagnosisRecommendation update(DiagnosisRecommendation diagnosisRecommendation) {
        diagnosisRecommendationMapper.update(diagnosisRecommendation);
        return diagnosisRecommendation;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return diagnosisRecommendationMapper.deleteById(tenantId, id) > 0;
    }
}

