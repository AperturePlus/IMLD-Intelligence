package xenosoft.imldintelligence.module.diagnoses.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis.DiagnosisSessionMapper;
import xenosoft.imldintelligence.module.diagnoses.model.DiagnosisSession;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DiagnosisSessionRepositoryImpl implements DiagnosisSessionRepository {
    private final DiagnosisSessionMapper diagnosisSessionMapper;

    @Override
    public Optional<DiagnosisSession> findById(Long tenantId, Long id) {
        return Optional.ofNullable(diagnosisSessionMapper.findById(tenantId, id));
    }

    @Override
    public List<DiagnosisSession> listByTenantId(Long tenantId) {
        return diagnosisSessionMapper.listByTenantId(tenantId);
    }

    @Override
    public List<DiagnosisSession> listByPatientId(Long tenantId, Long patientId) {
        return diagnosisSessionMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public List<DiagnosisSession> listByEncounterId(Long tenantId, Long encounterId) {
        return diagnosisSessionMapper.listByEncounterId(tenantId, encounterId);
    }

    @Override
    public DiagnosisSession save(DiagnosisSession diagnosisSession) {
        diagnosisSessionMapper.insert(diagnosisSession);
        return diagnosisSession;
    }

    @Override
    public DiagnosisSession update(DiagnosisSession diagnosisSession) {
        diagnosisSessionMapper.update(diagnosisSession);
        return diagnosisSession;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return diagnosisSessionMapper.deleteById(tenantId, id) > 0;
    }
}

