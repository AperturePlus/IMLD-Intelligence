package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientExternalIdRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.PatientExternalIdMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 患者外部标识仓储实现类，基于 MyBatis-Plus 完成患者外部标识的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class PatientExternalIdRepositoryImpl implements PatientExternalIdRepository {
    private final PatientExternalIdMapper patientExternalIdMapper;

    @Override
    public Optional<PatientExternalId> findById(Long tenantId, Long id) {
        return Optional.ofNullable(patientExternalIdMapper.selectOne(new LambdaQueryWrapper<PatientExternalId>()
                .eq(PatientExternalId::getTenantId, tenantId)
                .eq(PatientExternalId::getId, id)));
    }

    @Override
    public Optional<PatientExternalId> findByIdTypeAndIdValue(Long tenantId, String idType, String idValue) {
        return Optional.ofNullable(patientExternalIdMapper.selectOne(new LambdaQueryWrapper<PatientExternalId>()
                .eq(PatientExternalId::getTenantId, tenantId)
                .eq(PatientExternalId::getIdType, idType)
                .eq(PatientExternalId::getIdValue, idValue)));
    }

    @Override
    public List<PatientExternalId> listByPatientId(Long tenantId, Long patientId) {
        return patientExternalIdMapper.selectList(new LambdaQueryWrapper<PatientExternalId>()
                .eq(PatientExternalId::getTenantId, tenantId)
                .eq(PatientExternalId::getPatientId, patientId)
                .orderByDesc(PatientExternalId::getId));
    }

    @Override
    public List<PatientExternalId> listByTenantId(Long tenantId) {
        return patientExternalIdMapper.selectList(new LambdaQueryWrapper<PatientExternalId>()
                .eq(PatientExternalId::getTenantId, tenantId)
                .orderByDesc(PatientExternalId::getId));
    }

    @Override
    public PatientExternalId save(PatientExternalId patientExternalId) {
        patientExternalIdMapper.insert(patientExternalId);
        return patientExternalId;
    }

    @Override
    public PatientExternalId update(PatientExternalId patientExternalId) {
        patientExternalIdMapper.update(null, new LambdaUpdateWrapper<PatientExternalId>()
                .eq(PatientExternalId::getTenantId, patientExternalId.getTenantId())
                .eq(PatientExternalId::getId, patientExternalId.getId())
                .set(PatientExternalId::getPatientId, patientExternalId.getPatientId())
                .set(PatientExternalId::getIdType, patientExternalId.getIdType())
                .set(PatientExternalId::getIdValue, patientExternalId.getIdValue())
                .set(PatientExternalId::getSourceOrg, patientExternalId.getSourceOrg())
                .set(PatientExternalId::getIsPrimary, patientExternalId.getIsPrimary()));
        return patientExternalId;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return patientExternalIdMapper.delete(new LambdaQueryWrapper<PatientExternalId>()
                .eq(PatientExternalId::getTenantId, tenantId)
                .eq(PatientExternalId::getId, id)) > 0;
    }
}
