package xenosoft.imldintelligence.module.integration.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.integration.internal.model.IntegrationJob;
import xenosoft.imldintelligence.module.integration.internal.repository.IntegrationJobRepository;
import xenosoft.imldintelligence.module.integration.internal.repository.mybatis.IntegrationJobMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IntegrationJobRepositoryImpl implements IntegrationJobRepository {
    private final IntegrationJobMapper integrationJobMapper;

    @Override
    public Optional<IntegrationJob> findById(Long tenantId, Long id) {
        return Optional.ofNullable(integrationJobMapper.findById(tenantId, id));
    }

    @Override
    public Optional<IntegrationJob> findByJobNo(Long tenantId, String jobNo) {
        return Optional.ofNullable(integrationJobMapper.findByJobNo(tenantId, jobNo));
    }

    @Override
    public List<IntegrationJob> listByTenantId(Long tenantId) {
        return integrationJobMapper.listByTenantId(tenantId);
    }

    @Override
    public List<IntegrationJob> listBySourceSystem(Long tenantId, String sourceSystem) {
        return integrationJobMapper.listBySourceSystem(tenantId, sourceSystem);
    }

    @Override
    public IntegrationJob save(IntegrationJob integrationJob) {
        integrationJobMapper.insert(integrationJob);
        return integrationJob;
    }

    @Override
    public IntegrationJob update(IntegrationJob integrationJob) {
        integrationJobMapper.update(integrationJob);
        return integrationJob;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return integrationJobMapper.deleteById(tenantId, id) > 0;
    }
}

