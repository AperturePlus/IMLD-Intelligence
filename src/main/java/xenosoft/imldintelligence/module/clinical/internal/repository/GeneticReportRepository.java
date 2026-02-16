package xenosoft.imldintelligence.module.clinical.internal.repository;

import xenosoft.imldintelligence.module.clinical.internal.model.GeneticReport;

import java.util.List;
import java.util.Optional;

public interface GeneticReportRepository {
    Optional<GeneticReport> findById(Long tenantId, Long id);

    List<GeneticReport> listByTenantId(Long tenantId);

    List<GeneticReport> listByPatientId(Long tenantId, Long patientId);

    List<GeneticReport> listByEncounterId(Long tenantId, Long encounterId);

    GeneticReport save(GeneticReport geneticReport);

    GeneticReport update(GeneticReport geneticReport);

    Boolean deleteById(Long tenantId, Long id);
}

