package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.repository.PatientReportedDataRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.PatientReportedDataMapper;
import xenosoft.imldintelligence.module.careplan.model.PatientReportedData;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PatientReportedDataRepositoryImpl implements PatientReportedDataRepository {
    private final PatientReportedDataMapper patientReportedDataMapper;

    @Override
    public Optional<PatientReportedData> findById(Long tenantId, Long id) {
        return Optional.ofNullable(patientReportedDataMapper.findById(tenantId, id));
    }

    @Override
    public List<PatientReportedData> listByTenantId(Long tenantId) {
        return patientReportedDataMapper.listByTenantId(tenantId);
    }

    @Override
    public List<PatientReportedData> listByCarePlanId(Long tenantId, Long carePlanId) {
        return patientReportedDataMapper.listByCarePlanId(tenantId, carePlanId);
    }

    @Override
    public List<PatientReportedData> listByPatientId(Long tenantId, Long patientId) {
        return patientReportedDataMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public PatientReportedData save(PatientReportedData patientReportedData) {
        patientReportedDataMapper.insert(patientReportedData);
        return patientReportedData;
    }

    @Override
    public PatientReportedData update(PatientReportedData patientReportedData) {
        patientReportedDataMapper.update(patientReportedData);
        return patientReportedData;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return patientReportedDataMapper.deleteById(tenantId, id) > 0;
    }
}

