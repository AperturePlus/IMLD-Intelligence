package xenosoft.imldintelligence.module.integration.internal.repository;

import xenosoft.imldintelligence.module.integration.internal.model.IntegrationJob;

import java.util.List;
import java.util.Optional;

public interface IntegrationJobRepository {
    Optional<IntegrationJob> findById(Long tenantId, Long id);

    Optional<IntegrationJob> findByJobNo(Long tenantId, String jobNo);

    List<IntegrationJob> listByTenantId(Long tenantId);

    List<IntegrationJob> listBySourceSystem(Long tenantId, String sourceSystem);

    IntegrationJob save(IntegrationJob integrationJob);

    IntegrationJob update(IntegrationJob integrationJob);

    Boolean deleteById(Long tenantId, Long id);
}

