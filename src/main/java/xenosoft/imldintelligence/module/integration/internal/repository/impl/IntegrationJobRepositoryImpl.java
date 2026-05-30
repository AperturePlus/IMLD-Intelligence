package xenosoft.imldintelligence.module.integration.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.integration.internal.model.IntegrationJob;
import xenosoft.imldintelligence.module.integration.internal.repository.IntegrationJobRepository;
import xenosoft.imldintelligence.module.integration.internal.repository.mybatis.IntegrationJobMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 集成任务仓储实现类，基于 MyBatis-Plus 完成集成任务的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class IntegrationJobRepositoryImpl implements IntegrationJobRepository {
    private final IntegrationJobMapper integrationJobMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<IntegrationJob> findById(Long tenantId, Long id) {
        return Optional.ofNullable(integrationJobMapper.selectOne(new LambdaQueryWrapper<IntegrationJob>()
                .eq(IntegrationJob::getTenantId, tenantId)
                .eq(IntegrationJob::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<IntegrationJob> findByJobNo(Long tenantId, String jobNo) {
        return Optional.ofNullable(integrationJobMapper.selectOne(new LambdaQueryWrapper<IntegrationJob>()
                .eq(IntegrationJob::getTenantId, tenantId)
                .eq(IntegrationJob::getJobNo, jobNo)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IntegrationJob> listByTenantId(Long tenantId) {
        return integrationJobMapper.selectList(new LambdaQueryWrapper<IntegrationJob>()
                .eq(IntegrationJob::getTenantId, tenantId)
                .orderByDesc(IntegrationJob::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IntegrationJob> listBySourceSystem(Long tenantId, String sourceSystem) {
        return integrationJobMapper.selectList(new LambdaQueryWrapper<IntegrationJob>()
                .eq(IntegrationJob::getTenantId, tenantId)
                .eq(IntegrationJob::getSourceSystem, sourceSystem)
                .orderByDesc(IntegrationJob::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IntegrationJob save(IntegrationJob integrationJob) {
        integrationJobMapper.insert(integrationJob);
        return integrationJob;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IntegrationJob update(IntegrationJob integrationJob) {
        integrationJobMapper.update(integrationJob, new LambdaUpdateWrapper<IntegrationJob>()
                .eq(IntegrationJob::getTenantId, integrationJob.getTenantId())
                .eq(IntegrationJob::getId, integrationJob.getId()));
        return integrationJob;
    }

    @Override
    public boolean updateStatusIfCurrent(Long tenantId,
                                         Long id,
                                         String expectedStatus,
                                         String targetStatus,
                                         OffsetDateTime finishedAt,
                                         String errorMessage) {
        LambdaUpdateWrapper<IntegrationJob> wrapper = new LambdaUpdateWrapper<IntegrationJob>()
                .eq(IntegrationJob::getTenantId, tenantId)
                .eq(IntegrationJob::getId, id)
                .eq(IntegrationJob::getStatus, expectedStatus)
                .set(IntegrationJob::getStatus, targetStatus)
                .set(IntegrationJob::getFinishedAt, finishedAt)
                .set(IntegrationJob::getErrorMessage, errorMessage);
        return integrationJobMapper.update(null, wrapper) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return integrationJobMapper.delete(new LambdaQueryWrapper<IntegrationJob>()
                .eq(IntegrationJob::getTenantId, tenantId)
                .eq(IntegrationJob::getId, id)) > 0;
    }
}
