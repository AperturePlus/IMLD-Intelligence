package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.model.FollowupTask;
import xenosoft.imldintelligence.module.careplan.internal.repository.FollowupTaskRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.FollowupTaskMapper;

import java.util.List;
import java.util.Optional;

/**
 * 随访任务仓储实现类，基于 MyBatis-Plus 完成随访任务的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class FollowupTaskRepositoryImpl implements FollowupTaskRepository {
    private final FollowupTaskMapper followupTaskMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FollowupTask> findById(Long tenantId, Long id) {
        return Optional.ofNullable(followupTaskMapper.selectOne(new LambdaQueryWrapper<FollowupTask>()
                .eq(FollowupTask::getTenantId, tenantId)
                .eq(FollowupTask::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FollowupTask> listByTenantId(Long tenantId) {
        return followupTaskMapper.selectList(new LambdaQueryWrapper<FollowupTask>()
                .eq(FollowupTask::getTenantId, tenantId)
                .orderByDesc(FollowupTask::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FollowupTask> listByCarePlanId(Long tenantId, Long carePlanId) {
        return followupTaskMapper.selectList(new LambdaQueryWrapper<FollowupTask>()
                .eq(FollowupTask::getTenantId, tenantId)
                .eq(FollowupTask::getCarePlanId, carePlanId)
                .orderByDesc(FollowupTask::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FollowupTask> listByPatientId(Long tenantId, Long patientId) {
        return followupTaskMapper.selectList(new LambdaQueryWrapper<FollowupTask>()
                .eq(FollowupTask::getTenantId, tenantId)
                .eq(FollowupTask::getPatientId, patientId)
                .orderByDesc(FollowupTask::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FollowupTask save(FollowupTask followupTask) {
        followupTaskMapper.insert(followupTask);
        return followupTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FollowupTask update(FollowupTask followupTask) {
        followupTaskMapper.update(followupTask, new LambdaUpdateWrapper<FollowupTask>()
                .eq(FollowupTask::getTenantId, followupTask.getTenantId())
                .eq(FollowupTask::getId, followupTask.getId()));
        return followupTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return followupTaskMapper.delete(new LambdaQueryWrapper<FollowupTask>()
                .eq(FollowupTask::getTenantId, tenantId)
                .eq(FollowupTask::getId, id)) > 0;
    }
}
