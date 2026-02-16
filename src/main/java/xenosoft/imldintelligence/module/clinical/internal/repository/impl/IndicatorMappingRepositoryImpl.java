package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorMapping;
import xenosoft.imldintelligence.module.clinical.internal.repository.IndicatorMappingRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.mybatis.IndicatorMappingMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IndicatorMappingRepositoryImpl implements IndicatorMappingRepository {
    private final IndicatorMappingMapper indicatorMappingMapper;

    @Override
    public Optional<IndicatorMapping> findById(Long tenantId, Long id) {
        return Optional.ofNullable(indicatorMappingMapper.findById(tenantId, id));
    }

    @Override
    public Optional<IndicatorMapping> findBySourceSystemAndSourceCode(Long tenantId, String sourceSystem, String sourceCode) {
        return Optional.ofNullable(indicatorMappingMapper.findBySourceSystemAndSourceCode(tenantId, sourceSystem, sourceCode));
    }

    @Override
    public List<IndicatorMapping> listByTenantId(Long tenantId) {
        return indicatorMappingMapper.listByTenantId(tenantId);
    }

    @Override
    public List<IndicatorMapping> listByTargetIndicatorCode(Long tenantId, String targetIndicatorCode) {
        return indicatorMappingMapper.listByTargetIndicatorCode(tenantId, targetIndicatorCode);
    }

    @Override
    public IndicatorMapping save(IndicatorMapping indicatorMapping) {
        indicatorMappingMapper.insert(indicatorMapping);
        return indicatorMapping;
    }

    @Override
    public IndicatorMapping update(IndicatorMapping indicatorMapping) {
        indicatorMappingMapper.update(indicatorMapping);
        return indicatorMapping;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return indicatorMappingMapper.deleteById(tenantId, id) > 0;
    }
}

