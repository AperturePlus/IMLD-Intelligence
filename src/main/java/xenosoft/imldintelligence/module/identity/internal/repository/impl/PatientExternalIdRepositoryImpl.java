package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.PatientExternalIdRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.PatientExternalIdMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;

import java.util.List;
import java.util.Optional;

/**
 * 患者外部标识仓储实现类，基于 MyBatis Mapper 完成患者外部标识的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class PatientExternalIdRepositoryImpl implements PatientExternalIdRepository {
    private final PatientExternalIdMapper patientExternalIdMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PatientExternalId> findById(Long tenantId, Long id) {
        return Optional.ofNullable(patientExternalIdMapper.findById(tenantId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PatientExternalId> findByIdTypeAndIdValue(Long tenantId, String idType, String idValue) {
        return Optional.ofNullable(patientExternalIdMapper.findByIdTypeAndIdValue(tenantId, idType, idValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PatientExternalId> listByPatientId(Long tenantId, Long patientId) {
        return patientExternalIdMapper.listByPatientId(tenantId, patientId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PatientExternalId> listByTenantId(Long tenantId) {
        return patientExternalIdMapper.listByTenantId(tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PatientExternalId save(PatientExternalId patientExternalId) {
        patientExternalIdMapper.insert(patientExternalId);
        return patientExternalId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PatientExternalId update(PatientExternalId patientExternalId) {
        patientExternalIdMapper.update(patientExternalId);
        return patientExternalId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return patientExternalIdMapper.deleteById(tenantId, id) > 0;
    }
}
