package xenosoft.imldintelligence.module.clinical.internal.repository;

import xenosoft.imldintelligence.module.clinical.internal.model.ClinicalHistoryEntry;

import java.util.List;
import java.util.Optional;

public interface ClinicalHistoryEntryRepository {
    Optional<ClinicalHistoryEntry> findById(Long tenantId, Long id);

    List<ClinicalHistoryEntry> listByTenantId(Long tenantId);

    List<ClinicalHistoryEntry> listByPatientId(Long tenantId, Long patientId);

    List<ClinicalHistoryEntry> listByEncounterId(Long tenantId, Long encounterId);

    ClinicalHistoryEntry save(ClinicalHistoryEntry clinicalHistoryEntry);

    ClinicalHistoryEntry update(ClinicalHistoryEntry clinicalHistoryEntry);

    Boolean deleteById(Long tenantId, Long id);
}

