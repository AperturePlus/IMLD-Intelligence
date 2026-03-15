package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.clinical.internal.model.LabResult;
import xenosoft.imldintelligence.module.clinical.internal.repository.LabResultRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.mybatis.LabResultMapper;

import java.util.List;
import java.util.Optional;

/**
 * 检验结果仓储实现类，基于 MyBatis-Plus 完成检验结果的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class LabResultRepositoryImpl implements LabResultRepository {
    private final LabResultMapper labResultMapper;

    @Override
    public Optional<LabResult> findById(Long tenantId, Long id) {
        return Optional.ofNullable(labResultMapper.selectOne(new LambdaQueryWrapper<LabResult>()
                .eq(LabResult::getTenantId, tenantId)
                .eq(LabResult::getId, id)));
    }

    @Override
    public List<LabResult> listByTenantId(Long tenantId) {
        return labResultMapper.selectList(new LambdaQueryWrapper<LabResult>()
                .eq(LabResult::getTenantId, tenantId)
                .orderByDesc(LabResult::getId));
    }

    @Override
    public List<LabResult> listByPatientId(Long tenantId, Long patientId) {
        return labResultMapper.selectList(new LambdaQueryWrapper<LabResult>()
                .eq(LabResult::getTenantId, tenantId)
                .eq(LabResult::getPatientId, patientId)
                .orderByDesc(LabResult::getId));
    }

    @Override
    public List<LabResult> listByEncounterId(Long tenantId, Long encounterId) {
        return labResultMapper.selectList(new LambdaQueryWrapper<LabResult>()
                .eq(LabResult::getTenantId, tenantId)
                .eq(LabResult::getEncounterId, encounterId)
                .orderByDesc(LabResult::getId));
    }

    @Override
    public LabResult save(LabResult labResult) {
        labResultMapper.insert(labResult);
        return labResult;
    }

    @Override
    public LabResult update(LabResult labResult) {
        labResultMapper.update(labResult, new LambdaUpdateWrapper<LabResult>()
                .eq(LabResult::getTenantId, labResult.getTenantId())
                .eq(LabResult::getId, labResult.getId()));
        return labResult;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return labResultMapper.delete(new LambdaQueryWrapper<LabResult>()
                .eq(LabResult::getTenantId, tenantId)
                .eq(LabResult::getId, id)) > 0;
    }
}
