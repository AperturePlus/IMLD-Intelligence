package xenosoft.imldintelligence.module.diagnoses.internal.repository;

import xenosoft.imldintelligence.module.diagnoses.model.ModelRegistry;

import java.util.List;
import java.util.Optional;

public interface ModelRegistryRepository {
    Optional<ModelRegistry> findById(Long tenantId, Long id);

    Optional<ModelRegistry> findByModelCodeAndModelVersion(Long tenantId, String modelCode, String modelVersion);

    List<ModelRegistry> listByTenantId(Long tenantId);

    ModelRegistry save(ModelRegistry modelRegistry);

    ModelRegistry update(ModelRegistry modelRegistry);

    Boolean deleteById(Long tenantId, Long id);
}

