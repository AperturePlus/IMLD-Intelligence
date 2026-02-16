package xenosoft.imldintelligence.module.clinical.internal.repository;

import xenosoft.imldintelligence.module.clinical.internal.model.ImagingReport;

import java.util.List;
import java.util.Optional;

public interface ImagingReportRepository {
    Optional<ImagingReport> findById(Long tenantId, Long id);

    List<ImagingReport> listByTenantId(Long tenantId);

    List<ImagingReport> listByPatientId(Long tenantId, Long patientId);

    List<ImagingReport> listByEncounterId(Long tenantId, Long encounterId);

    ImagingReport save(ImagingReport imagingReport);

    ImagingReport update(ImagingReport imagingReport);

    Boolean deleteById(Long tenantId, Long id);
}

