package xenosoft.imldintelligence.module.clinical.internal.repository;

import xenosoft.imldintelligence.module.clinical.internal.model.LabResult;

import java.util.List;
import java.util.Optional;

public interface LabResultRepository {
    Optional<LabResult> findById(Long tenantId, Long id);

    List<LabResult> listByTenantId(Long tenantId);

    List<LabResult> listByPatientId(Long tenantId, Long patientId);

    List<LabResult> listByEncounterId(Long tenantId, Long encounterId);

    LabResult save(LabResult labResult);

    LabResult update(LabResult labResult);

    Boolean deleteById(Long tenantId, Long id);
}

