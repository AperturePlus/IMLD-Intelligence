package xenosoft.imldintelligence.module.diagnoses.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DoctorFeedbackRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis.DoctorFeedbackMapper;
import xenosoft.imldintelligence.module.diagnoses.model.DoctorFeedback;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DoctorFeedbackRepositoryImpl implements DoctorFeedbackRepository {
    private final DoctorFeedbackMapper doctorFeedbackMapper;

    @Override
    public Optional<DoctorFeedback> findById(Long tenantId, Long id) {
        return Optional.ofNullable(doctorFeedbackMapper.findById(tenantId, id));
    }

    @Override
    public List<DoctorFeedback> listByTenantId(Long tenantId) {
        return doctorFeedbackMapper.listByTenantId(tenantId);
    }

    @Override
    public List<DoctorFeedback> listBySessionId(Long tenantId, Long sessionId) {
        return doctorFeedbackMapper.listBySessionId(tenantId, sessionId);
    }

    @Override
    public List<DoctorFeedback> listByResultId(Long tenantId, Long resultId) {
        return doctorFeedbackMapper.listByResultId(tenantId, resultId);
    }

    @Override
    public DoctorFeedback save(DoctorFeedback doctorFeedback) {
        doctorFeedbackMapper.insert(doctorFeedback);
        return doctorFeedback;
    }

    @Override
    public DoctorFeedback update(DoctorFeedback doctorFeedback) {
        doctorFeedbackMapper.update(doctorFeedback);
        return doctorFeedback;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return doctorFeedbackMapper.deleteById(tenantId, id) > 0;
    }
}

