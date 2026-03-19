package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;
import xenosoft.imldintelligence.module.identity.internal.repository.EncounterRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.EncounterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 就诊记录仓储实现类，基于 MyBatis-Plus 完成就诊记录的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class EncounterRepositoryImpl implements EncounterRepository {
    private final EncounterMapper encounterMapper;

    @Override
    public Optional<Encounter> findById(Long tenantId, Long id) {
        return Optional.ofNullable(encounterMapper.selectOne(new LambdaQueryWrapper<Encounter>()
                .eq(Encounter::getTenantId, tenantId)
                .eq(Encounter::getId, id)));
    }

    @Override
    public Optional<Encounter> findByEncounterNo(Long tenantId, String encounterNo) {
        return Optional.ofNullable(encounterMapper.selectOne(new LambdaQueryWrapper<Encounter>()
                .eq(Encounter::getTenantId, tenantId)
                .eq(Encounter::getEncounterNo, encounterNo)));
    }

    @Override
    public List<Encounter> listByTenantId(Long tenantId) {
        return encounterMapper.selectList(new LambdaQueryWrapper<Encounter>()
                .eq(Encounter::getTenantId, tenantId)
                .orderByDesc(Encounter::getId));
    }

    @Override
    public List<Encounter> listByPatientId(Long tenantId, Long patientId) {
        return encounterMapper.selectList(new LambdaQueryWrapper<Encounter>()
                .eq(Encounter::getTenantId, tenantId)
                .eq(Encounter::getPatientId, patientId)
                .orderByDesc(Encounter::getId));
    }

    @Override
    public Encounter save(Encounter encounter) {
        encounterMapper.insert(encounter);
        return encounter;
    }

    @Override
    public Encounter update(Encounter encounter) {
        encounterMapper.update(null, new LambdaUpdateWrapper<Encounter>()
                .eq(Encounter::getTenantId, encounter.getTenantId())
                .eq(Encounter::getId, encounter.getId())
                .set(Encounter::getPatientId, encounter.getPatientId())
                .set(Encounter::getEncounterNo, encounter.getEncounterNo())
                .set(Encounter::getEncounterType, encounter.getEncounterType())
                .set(Encounter::getDeptName, encounter.getDeptName())
                .set(Encounter::getAttendingDoctorId, encounter.getAttendingDoctorId())
                .set(Encounter::getStartAt, encounter.getStartAt())
                .set(Encounter::getEndAt, encounter.getEndAt())
                .set(Encounter::getSourceSystem, encounter.getSourceSystem()));
        return encounter;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return encounterMapper.delete(new LambdaQueryWrapper<Encounter>()
                .eq(Encounter::getTenantId, tenantId)
                .eq(Encounter::getId, id)) > 0;
    }
}
