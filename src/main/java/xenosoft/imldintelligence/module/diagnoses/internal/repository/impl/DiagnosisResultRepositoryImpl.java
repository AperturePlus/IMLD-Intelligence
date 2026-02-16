package xenosoft.imldintelligence.module.diagnoses.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisResultRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis.DiagnosisResultMapper;
import xenosoft.imldintelligence.module.diagnoses.model.DiagnosisResult;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DiagnosisResultRepositoryImpl implements DiagnosisResultRepository {
    private final DiagnosisResultMapper diagnosisResultMapper;

    @Override
    public Optional<DiagnosisResult> findById(Long tenantId, Long id) {
        return Optional.ofNullable(diagnosisResultMapper.findById(tenantId, id));
    }

    @Override
    public List<DiagnosisResult> listByTenantId(Long tenantId) {
        return diagnosisResultMapper.listByTenantId(tenantId);
    }

    @Override
    public List<DiagnosisResult> listBySessionId(Long tenantId, Long sessionId) {
        return diagnosisResultMapper.listBySessionId(tenantId, sessionId);
    }

    @Override
    public DiagnosisResult save(DiagnosisResult diagnosisResult) {
        diagnosisResultMapper.insert(diagnosisResult);
        return diagnosisResult;
    }

    @Override
    public DiagnosisResult update(DiagnosisResult diagnosisResult) {
        diagnosisResultMapper.update(diagnosisResult);
        return diagnosisResult;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return diagnosisResultMapper.deleteById(tenantId, id) > 0;
    }
}

