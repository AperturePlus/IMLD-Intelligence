package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.clinical.internal.model.ImagingReport;
import xenosoft.imldintelligence.module.clinical.internal.repository.ImagingReportRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.mybatis.ImagingReportMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImagingReportRepositoryImpl implements ImagingReportRepository {
    private final ImagingReportMapper imagingReportMapper;

    @Override
    public Optional<ImagingReport> findById(Long tenantId, Long id) {
        return Optional.ofNullable(imagingReportMapper.findById(tenantId, id));
    }

    @Override
    public List<ImagingReport> listByTenantId(Long tenantId) {
        return imagingReportMapper.listByTenantId(tenantId);
    }

    @Override
    public List<ImagingReport> listByPatientId(Long tenantId, Long patientId) {
        return imagingReportMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public List<ImagingReport> listByEncounterId(Long tenantId, Long encounterId) {
        return imagingReportMapper.listByEncounterId(tenantId, encounterId);
    }

    @Override
    public ImagingReport save(ImagingReport imagingReport) {
        imagingReportMapper.insert(imagingReport);
        return imagingReport;
    }

    @Override
    public ImagingReport update(ImagingReport imagingReport) {
        imagingReportMapper.update(imagingReport);
        return imagingReport;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return imagingReportMapper.deleteById(tenantId, id) > 0;
    }
}

