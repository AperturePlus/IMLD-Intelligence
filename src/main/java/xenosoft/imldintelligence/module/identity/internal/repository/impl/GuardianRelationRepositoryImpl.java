package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.GuardianRelationRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.GuardianRelationMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GuardianRelationRepositoryImpl implements GuardianRelationRepository {
    private final GuardianRelationMapper guardianRelationMapper;

    @Override
    public Optional<GuardianRelation> findById(Long tenantId, Long id) {
        return Optional.ofNullable(guardianRelationMapper.findById(tenantId, id));
    }

    @Override
    public Optional<GuardianRelation> findPrimaryByPatientId(Long tenantId, Long patientId) {
        return Optional.ofNullable(guardianRelationMapper.findPrimaryByPatientId(tenantId, patientId));
    }

    @Override
    public List<GuardianRelation> listByPatientId(Long tenantId, Long patientId) {
        return guardianRelationMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public List<GuardianRelation> listByTenantId(Long tenantId) {
        return guardianRelationMapper.listByTenantId(tenantId);
    }

    @Override
    public GuardianRelation save(GuardianRelation guardianRelation) {
        guardianRelationMapper.insert(guardianRelation);
        return guardianRelation;
    }

    @Override
    public GuardianRelation update(GuardianRelation guardianRelation) {
        guardianRelationMapper.update(guardianRelation);
        return guardianRelation;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return guardianRelationMapper.deleteById(tenantId, id) > 0;
    }
}
