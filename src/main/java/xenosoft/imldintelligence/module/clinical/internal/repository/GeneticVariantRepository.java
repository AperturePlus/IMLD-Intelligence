package xenosoft.imldintelligence.module.clinical.internal.repository;

import xenosoft.imldintelligence.module.clinical.internal.model.GeneticVariant;

import java.util.List;
import java.util.Optional;

public interface GeneticVariantRepository {
    Optional<GeneticVariant> findById(Long tenantId, Long id);

    List<GeneticVariant> listByTenantId(Long tenantId);

    List<GeneticVariant> listByReportId(Long tenantId, Long reportId);

    GeneticVariant save(GeneticVariant geneticVariant);

    GeneticVariant update(GeneticVariant geneticVariant);

    Boolean deleteById(Long tenantId, Long id);
}

