package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.PatientMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PatientRepositoryImpl implements PatientRepository {
    private final PatientMapper patientMapper;

    @Override
    public Optional<Patient> findById(Long tenantId, Long patientId) {
        return Optional.ofNullable(patientMapper.findById(tenantId, patientId));
    }

    @Override
    public Optional<Patient> findByPatientNo(Long tenantId, String patientNo) {
        return Optional.ofNullable(patientMapper.findByPatientNo(tenantId, patientNo));
    }

    @Override
    public List<Patient> listByTenantId(Long tenantId) {
        return patientMapper.listByTenantId(tenantId);
    }

    @Override
    public Patient save(Patient patient) {
        patientMapper.insert(patient);
        return patient;
    }

    @Override
    public Patient update(Patient patient) {
        patientMapper.update(patient);
        return patient;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long patientId) {
        return patientMapper.deleteById(tenantId, patientId) > 0;
    }

    @Override
    public Boolean deleteByPatientNo(Long tenantId, String patientNo) {
        return patientMapper.deleteByPatientNo(tenantId, patientNo) > 0;
    }
}
