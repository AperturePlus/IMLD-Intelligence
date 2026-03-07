package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.GuardianRelationRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.GuardianRelationMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;

import java.util.List;
import java.util.Optional;

/**
 * 监护人关系仓储实现类，基于 MyBatis Mapper 完成监护人关系的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class GuardianRelationRepositoryImpl implements GuardianRelationRepository {
    private final GuardianRelationMapper guardianRelationMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GuardianRelation> findById(Long tenantId, Long id) {
        return Optional.ofNullable(guardianRelationMapper.findById(tenantId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GuardianRelation> findPrimaryByPatientId(Long tenantId, Long patientId) {
        return Optional.ofNullable(guardianRelationMapper.findPrimaryByPatientId(tenantId, patientId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GuardianRelation> listByPatientId(Long tenantId, Long patientId) {
        return guardianRelationMapper.listByPatientId(tenantId, patientId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GuardianRelation> listByTenantId(Long tenantId) {
        return guardianRelationMapper.listByTenantId(tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuardianRelation save(GuardianRelation guardianRelation) {
        guardianRelationMapper.insert(guardianRelation);
        return guardianRelation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuardianRelation update(GuardianRelation guardianRelation) {
        guardianRelationMapper.update(guardianRelation);
        return guardianRelation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return guardianRelationMapper.deleteById(tenantId, id) > 0;
    }
}
