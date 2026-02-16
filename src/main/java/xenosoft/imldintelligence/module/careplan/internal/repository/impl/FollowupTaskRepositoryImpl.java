package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.repository.FollowupTaskRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.FollowupTaskMapper;
import xenosoft.imldintelligence.module.careplan.model.FollowupTask;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FollowupTaskRepositoryImpl implements FollowupTaskRepository {
    private final FollowupTaskMapper followupTaskMapper;

    @Override
    public Optional<FollowupTask> findById(Long tenantId, Long id) {
        return Optional.ofNullable(followupTaskMapper.findById(tenantId, id));
    }

    @Override
    public List<FollowupTask> listByTenantId(Long tenantId) {
        return followupTaskMapper.listByTenantId(tenantId);
    }

    @Override
    public List<FollowupTask> listByCarePlanId(Long tenantId, Long carePlanId) {
        return followupTaskMapper.listByCarePlanId(tenantId, carePlanId);
    }

    @Override
    public List<FollowupTask> listByPatientId(Long tenantId, Long patientId) {
        return followupTaskMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public FollowupTask save(FollowupTask followupTask) {
        followupTaskMapper.insert(followupTask);
        return followupTask;
    }

    @Override
    public FollowupTask update(FollowupTask followupTask) {
        followupTaskMapper.update(followupTask);
        return followupTask;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return followupTaskMapper.deleteById(tenantId, id) > 0;
    }
}

