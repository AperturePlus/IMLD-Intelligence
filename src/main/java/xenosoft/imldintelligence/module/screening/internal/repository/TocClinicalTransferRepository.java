package xenosoft.imldintelligence.module.screening.internal.repository;

import xenosoft.imldintelligence.module.screening.model.TocClinicalTransfer;

import java.util.List;
import java.util.Optional;

public interface TocClinicalTransferRepository {
    Optional<TocClinicalTransfer> findById(Long tenantId, Long id);

    List<TocClinicalTransfer> listByTenantId(Long tenantId);

    List<TocClinicalTransfer> listByResponseId(Long tenantId, Long responseId);

    List<TocClinicalTransfer> listByPatientId(Long tenantId, Long patientId);

    TocClinicalTransfer save(TocClinicalTransfer tocClinicalTransfer);

    TocClinicalTransfer update(TocClinicalTransfer tocClinicalTransfer);

    Boolean deleteById(Long tenantId, Long id);
}

