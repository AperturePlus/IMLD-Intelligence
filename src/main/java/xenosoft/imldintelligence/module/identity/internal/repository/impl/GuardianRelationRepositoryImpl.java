package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.GuardianRelation;
import xenosoft.imldintelligence.module.identity.internal.repository.GuardianRelationRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.GuardianRelationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 监护人关系仓储实现类，基于 MyBatis-Plus 完成监护人关系的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class GuardianRelationRepositoryImpl implements GuardianRelationRepository {
    private final GuardianRelationMapper guardianRelationMapper;

    @Override
    public Optional<GuardianRelation> findById(Long tenantId, Long id) {
        return Optional.ofNullable(guardianRelationMapper.selectOne(new LambdaQueryWrapper<GuardianRelation>()
                .eq(GuardianRelation::getTenantId, tenantId)
                .eq(GuardianRelation::getId, id)));
    }

    @Override
    public Optional<GuardianRelation> findPrimaryByPatientId(Long tenantId, Long patientId) {
        return Optional.ofNullable(guardianRelationMapper.selectOne(new LambdaQueryWrapper<GuardianRelation>()
                .eq(GuardianRelation::getTenantId, tenantId)
                .eq(GuardianRelation::getPatientId, patientId)
                .eq(GuardianRelation::getIsPrimary, true)));
    }

    @Override
    public List<GuardianRelation> listByPatientId(Long tenantId, Long patientId) {
        return guardianRelationMapper.selectList(new LambdaQueryWrapper<GuardianRelation>()
                .eq(GuardianRelation::getTenantId, tenantId)
                .eq(GuardianRelation::getPatientId, patientId)
                .orderByDesc(GuardianRelation::getId));
    }

    @Override
    public List<GuardianRelation> listByTenantId(Long tenantId) {
        return guardianRelationMapper.selectList(new LambdaQueryWrapper<GuardianRelation>()
                .eq(GuardianRelation::getTenantId, tenantId)
                .orderByDesc(GuardianRelation::getId));
    }

    @Override
    public GuardianRelation save(GuardianRelation guardianRelation) {
        guardianRelationMapper.insert(guardianRelation);
        return guardianRelation;
    }

    @Override
    public GuardianRelation update(GuardianRelation guardianRelation) {
        guardianRelationMapper.update(null, new LambdaUpdateWrapper<GuardianRelation>()
                .eq(GuardianRelation::getTenantId, guardianRelation.getTenantId())
                .eq(GuardianRelation::getId, guardianRelation.getId())
                .set(GuardianRelation::getPatientId, guardianRelation.getPatientId())
                .set(GuardianRelation::getGuardianName, guardianRelation.getGuardianName())
                .set(GuardianRelation::getGuardianMobileEncrypted, guardianRelation.getGuardianMobileEncrypted())
                .set(GuardianRelation::getGuardianIdNoEncrypted, guardianRelation.getGuardianIdNoEncrypted())
                .set(GuardianRelation::getRelationType, guardianRelation.getRelationType())
                .set(GuardianRelation::getIsPrimary, guardianRelation.getIsPrimary())
                .set(GuardianRelation::getStatus, guardianRelation.getStatus()));
        return guardianRelation;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return guardianRelationMapper.update(null, new LambdaUpdateWrapper<GuardianRelation>()
                .eq(GuardianRelation::getTenantId, tenantId)
                .eq(GuardianRelation::getId, id)
                .set(GuardianRelation::getStatus, "INACTIVE")) > 0;
    }
}
