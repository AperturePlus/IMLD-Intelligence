package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorMapping;
import xenosoft.imldintelligence.module.clinical.internal.repository.IndicatorMappingRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.mybatis.IndicatorMappingMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IndicatorMappingRepositoryImpl implements IndicatorMappingRepository {
    private final IndicatorMappingMapper indicatorMappingMapper;

    @Override
    public Optional<IndicatorMapping> findById(Long tenantId, Long id) {
        return Optional.ofNullable(indicatorMappingMapper.selectOne(new LambdaQueryWrapper<IndicatorMapping>()
                .eq(IndicatorMapping::getTenantId, tenantId)
                .eq(IndicatorMapping::getId, id)));
    }

    @Override
    public Optional<IndicatorMapping> findBySourceSystemAndSourceCode(Long tenantId, String sourceSystem, String sourceCode) {
        return Optional.ofNullable(indicatorMappingMapper.selectOne(new LambdaQueryWrapper<IndicatorMapping>()
                .eq(IndicatorMapping::getTenantId, tenantId)
                .eq(IndicatorMapping::getSourceSystem, sourceSystem)
                .eq(IndicatorMapping::getSourceCode, sourceCode)));
    }

    @Override
    public List<IndicatorMapping> listByTenantId(Long tenantId) {
        return indicatorMappingMapper.selectList(new LambdaQueryWrapper<IndicatorMapping>()
                .eq(IndicatorMapping::getTenantId, tenantId)
                .orderByDesc(IndicatorMapping::getId));
    }

    @Override
    public List<IndicatorMapping> listByTargetIndicatorCode(Long tenantId, String targetIndicatorCode) {
        return indicatorMappingMapper.selectList(new LambdaQueryWrapper<IndicatorMapping>()
                .eq(IndicatorMapping::getTenantId, tenantId)
                .eq(IndicatorMapping::getTargetIndicatorCode, targetIndicatorCode)
                .orderByDesc(IndicatorMapping::getId));
    }

    @Override
    public IndicatorMapping save(IndicatorMapping indicatorMapping) {
        indicatorMappingMapper.insert(indicatorMapping);
        return indicatorMapping;
    }

    @Override
    public IndicatorMapping update(IndicatorMapping indicatorMapping) {
        indicatorMappingMapper.update(indicatorMapping, new LambdaUpdateWrapper<IndicatorMapping>()
                .eq(IndicatorMapping::getTenantId, indicatorMapping.getTenantId())
                .eq(IndicatorMapping::getId, indicatorMapping.getId()));
        return indicatorMapping;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return indicatorMappingMapper.update(null, new LambdaUpdateWrapper<IndicatorMapping>()
                .eq(IndicatorMapping::getTenantId, tenantId)
                .eq(IndicatorMapping::getId, id)
                .set(IndicatorMapping::getStatus, "INACTIVE")) > 0;
    }
}
