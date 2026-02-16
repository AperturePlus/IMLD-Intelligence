package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

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
        return Optional.ofNullable(geneticReportMapper.findById(tenantId, id));
    }

    @Override
    public List<GeneticReport> listByTenantId(Long tenantId) {
        return geneticReportMapper.listByTenantId(tenantId);
    }

    @Override
    public List<GeneticReport> listByPatientId(Long tenantId, Long patientId) {
        return geneticReportMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public List<GeneticReport> listByEncounterId(Long tenantId, Long encounterId) {
        return geneticReportMapper.listByEncounterId(tenantId, encounterId);
    }

    @Override
    public GeneticReport save(GeneticReport geneticReport) {
        geneticReportMapper.insert(geneticReport);
        return geneticReport;
    }

    @Override
    public GeneticReport update(GeneticReport geneticReport) {
        geneticReportMapper.update(geneticReport);
        return geneticReport;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return geneticReportMapper.deleteById(tenantId, id) > 0;
    }
}

