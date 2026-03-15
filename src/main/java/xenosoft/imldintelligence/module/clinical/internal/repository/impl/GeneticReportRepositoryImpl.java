package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticReport;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticReportRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.mybatis.GeneticReportMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GeneticReportRepositoryImpl implements GeneticReportRepository {
    private final GeneticReportMapper geneticReportMapper;

    @Override
    public Optional<GeneticReport> findById(Long tenantId, Long id) {
        return Optional.ofNullable(geneticReportMapper.selectOne(new LambdaQueryWrapper<GeneticReport>()
                .eq(GeneticReport::getTenantId, tenantId)
                .eq(GeneticReport::getId, id)));
    }

    @Override
    public List<GeneticReport> listByTenantId(Long tenantId) {
        return geneticReportMapper.selectList(new LambdaQueryWrapper<GeneticReport>()
                .eq(GeneticReport::getTenantId, tenantId)
                .orderByDesc(GeneticReport::getId));
    }

    @Override
    public List<GeneticReport> listByPatientId(Long tenantId, Long patientId) {
        return geneticReportMapper.selectList(new LambdaQueryWrapper<GeneticReport>()
                .eq(GeneticReport::getTenantId, tenantId)
                .eq(GeneticReport::getPatientId, patientId)
                .orderByDesc(GeneticReport::getId));
    }

    @Override
    public List<GeneticReport> listByEncounterId(Long tenantId, Long encounterId) {
        return geneticReportMapper.selectList(new LambdaQueryWrapper<GeneticReport>()
                .eq(GeneticReport::getTenantId, tenantId)
                .eq(GeneticReport::getEncounterId, encounterId)
                .orderByDesc(GeneticReport::getId));
    }

    @Override
    public GeneticReport save(GeneticReport geneticReport) {
        geneticReportMapper.insert(geneticReport);
        return geneticReport;
    }

    @Override
    public GeneticReport update(GeneticReport geneticReport) {
        geneticReportMapper.update(geneticReport, new LambdaUpdateWrapper<GeneticReport>()
                .eq(GeneticReport::getTenantId, geneticReport.getTenantId())
                .eq(GeneticReport::getId, geneticReport.getId()));
        return geneticReport;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return geneticReportMapper.delete(new LambdaQueryWrapper<GeneticReport>()
                .eq(GeneticReport::getTenantId, tenantId)
                .eq(GeneticReport::getId, id)) > 0;
    }
}
