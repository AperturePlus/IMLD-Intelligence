package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.EncounterRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.EncounterMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;

import java.util.List;
import java.util.Optional;

/**
 * 就诊记录仓储实现类，基于 MyBatis Mapper 完成就诊记录的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class EncounterRepositoryImpl implements EncounterRepository {
    private final EncounterMapper encounterMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Encounter> findById(Long tenantId, Long id) {
        return Optional.ofNullable(encounterMapper.findById(tenantId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Encounter> findByEncounterNo(Long tenantId, String encounterNo) {
        return Optional.ofNullable(encounterMapper.findByEncounterNo(tenantId, encounterNo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Encounter> listByTenantId(Long tenantId) {
        return encounterMapper.listByTenantId(tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Encounter> listByPatientId(Long tenantId, Long patientId) {
        return encounterMapper.listByPatientId(tenantId, patientId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Encounter save(Encounter encounter) {
        encounterMapper.insert(encounter);
        return encounter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Encounter update(Encounter encounter) {
        encounterMapper.update(encounter);
        return encounter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return encounterMapper.deleteById(tenantId, id) > 0;
    }
}
