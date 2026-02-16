package xenosoft.imldintelligence.module.clinical.internal.repository;

import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorMapping;

import java.util.List;
import java.util.Optional;

public interface IndicatorMappingRepository {
    Optional<IndicatorMapping> findById(Long tenantId, Long id);

    Optional<IndicatorMapping> findBySourceSystemAndSourceCode(Long tenantId, String sourceSystem, String sourceCode);

    List<IndicatorMapping> listByTenantId(Long tenantId);

    List<IndicatorMapping> listByTargetIndicatorCode(Long tenantId, String targetIndicatorCode);

    IndicatorMapping save(IndicatorMapping indicatorMapping);

    IndicatorMapping update(IndicatorMapping indicatorMapping);

    Boolean deleteById(Long tenantId, Long id);
}

