package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.PatientMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;

import java.util.List;
import java.util.Optional;

/**
 * 患者仓储实现类，基于 MyBatis Mapper 完成患者的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class PatientRepositoryImpl implements PatientRepository {
    private final PatientMapper patientMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Patient> findById(Long tenantId, Long patientId) {
        return Optional.ofNullable(patientMapper.findById(tenantId, patientId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Patient> findByPatientNo(Long tenantId, String patientNo) {
        return Optional.ofNullable(patientMapper.findByPatientNo(tenantId, patientNo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Patient> listByTenantId(Long tenantId) {
        return patientMapper.listByTenantId(tenantId);
    }

    @Override
    public long countByCondition(Long tenantId, String patientNo, String patientNameKeyword,
                                  String patientType, String status) {
        return patientMapper.countByCondition(tenantId, patientNo, patientNameKeyword, patientType, status);
    }

    @Override
    public List<Patient> listByCondition(Long tenantId, String patientNo, String patientNameKeyword,
                                          String patientType, String status, long offset, int limit) {
        return patientMapper.listByCondition(tenantId, patientNo, patientNameKeyword, patientType, status, offset, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Patient save(Patient patient) {
        patientMapper.insert(patient);
        return patient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Patient update(Patient patient) {
        patientMapper.update(patient);
        return patient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long patientId) {
        return patientMapper.deleteById(tenantId, patientId) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteByPatientNo(Long tenantId, String patientNo) {
        return patientMapper.deleteByPatientNo(tenantId, patientNo) > 0;
    }
}
