package xenosoft.imldintelligence.module.careplan.internal.repository;

import xenosoft.imldintelligence.module.careplan.model.PatientReportedData;

import java.util.List;
import java.util.Optional;

public interface PatientReportedDataRepository {
    Optional<PatientReportedData> findById(Long tenantId, Long id);

    List<PatientReportedData> listByTenantId(Long tenantId);

    List<PatientReportedData> listByCarePlanId(Long tenantId, Long carePlanId);

    List<PatientReportedData> listByPatientId(Long tenantId, Long patientId);

    PatientReportedData save(PatientReportedData patientReportedData);

    PatientReportedData update(PatientReportedData patientReportedData);

    Boolean deleteById(Long tenantId, Long id);
}

