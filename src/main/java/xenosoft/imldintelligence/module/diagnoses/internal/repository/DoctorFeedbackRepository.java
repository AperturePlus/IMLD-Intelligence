package xenosoft.imldintelligence.module.diagnoses.internal.repository;

import xenosoft.imldintelligence.module.diagnoses.model.DoctorFeedback;

import java.util.List;
import java.util.Optional;

public interface DoctorFeedbackRepository {
    Optional<DoctorFeedback> findById(Long tenantId, Long id);

    List<DoctorFeedback> listByTenantId(Long tenantId);

    List<DoctorFeedback> listBySessionId(Long tenantId, Long sessionId);

    List<DoctorFeedback> listByResultId(Long tenantId, Long resultId);

    DoctorFeedback save(DoctorFeedback doctorFeedback);

    DoctorFeedback update(DoctorFeedback doctorFeedback);

    Boolean deleteById(Long tenantId, Long id);
}

