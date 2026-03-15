package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticVariant;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticVariantRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.mybatis.GeneticVariantMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GeneticVariantRepositoryImpl implements GeneticVariantRepository {
    private final GeneticVariantMapper geneticVariantMapper;

    @Override
    public Optional<GeneticVariant> findById(Long tenantId, Long id) {
        return Optional.ofNullable(geneticVariantMapper.selectOne(new LambdaQueryWrapper<GeneticVariant>()
                .eq(GeneticVariant::getTenantId, tenantId)
                .eq(GeneticVariant::getId, id)));
    }

    @Override
    public List<GeneticVariant> listByTenantId(Long tenantId) {
        return geneticVariantMapper.selectList(new LambdaQueryWrapper<GeneticVariant>()
                .eq(GeneticVariant::getTenantId, tenantId)
                .orderByDesc(GeneticVariant::getId));
    }

    @Override
    public List<GeneticVariant> listByReportId(Long tenantId, Long reportId) {
        return geneticVariantMapper.selectList(new LambdaQueryWrapper<GeneticVariant>()
                .eq(GeneticVariant::getTenantId, tenantId)
                .eq(GeneticVariant::getReportId, reportId)
                .orderByDesc(GeneticVariant::getId));
    }

    @Override
    public GeneticVariant save(GeneticVariant geneticVariant) {
        geneticVariantMapper.insert(geneticVariant);
        return geneticVariant;
    }

    @Override
    public GeneticVariant update(GeneticVariant geneticVariant) {
        geneticVariantMapper.update(geneticVariant, new LambdaUpdateWrapper<GeneticVariant>()
                .eq(GeneticVariant::getTenantId, geneticVariant.getTenantId())
                .eq(GeneticVariant::getId, geneticVariant.getId()));
        return geneticVariant;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return geneticVariantMapper.delete(new LambdaQueryWrapper<GeneticVariant>()
                .eq(GeneticVariant::getTenantId, tenantId)
                .eq(GeneticVariant::getId, id)) > 0;
    }
}
