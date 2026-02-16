package xenosoft.imldintelligence.module.careplan.internal.repository;

import xenosoft.imldintelligence.module.careplan.model.FollowupTask;

import java.util.List;
import java.util.Optional;

public interface FollowupTaskRepository {
    Optional<FollowupTask> findById(Long tenantId, Long id);

    List<FollowupTask> listByTenantId(Long tenantId);

    List<FollowupTask> listByCarePlanId(Long tenantId, Long carePlanId);

    List<FollowupTask> listByPatientId(Long tenantId, Long patientId);

    FollowupTask save(FollowupTask followupTask);

    FollowupTask update(FollowupTask followupTask);

    Boolean deleteById(Long tenantId, Long id);
}

