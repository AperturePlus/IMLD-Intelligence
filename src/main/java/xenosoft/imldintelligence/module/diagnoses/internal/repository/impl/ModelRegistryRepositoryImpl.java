package xenosoft.imldintelligence.module.diagnoses.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.ModelRegistryRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis.ModelRegistryMapper;
import xenosoft.imldintelligence.module.diagnoses.model.ModelRegistry;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ModelRegistryRepositoryImpl implements ModelRegistryRepository {
    private final ModelRegistryMapper modelRegistryMapper;

    @Override
    public Optional<ModelRegistry> findById(Long tenantId, Long id) {
        return Optional.ofNullable(modelRegistryMapper.findById(tenantId, id));
    }

    @Override
    public Optional<ModelRegistry> findByModelCodeAndModelVersion(Long tenantId, String modelCode, String modelVersion) {
        return Optional.ofNullable(modelRegistryMapper.findByModelCodeAndModelVersion(tenantId, modelCode, modelVersion));
    }

    @Override
    public List<ModelRegistry> listByTenantId(Long tenantId) {
        return modelRegistryMapper.listByTenantId(tenantId);
    }

    @Override
    public ModelRegistry save(ModelRegistry modelRegistry) {
        modelRegistryMapper.insert(modelRegistry);
        return modelRegistry;
    }

    @Override
    public ModelRegistry update(ModelRegistry modelRegistry) {
        modelRegistryMapper.update(modelRegistry);
        return modelRegistry;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return modelRegistryMapper.deleteById(tenantId, id) > 0;
    }
}

