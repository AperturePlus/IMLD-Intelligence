package xenosoft.imldintelligence.module.clinical.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
        return Optional.ofNullable(imagingReportMapper.selectOne(new LambdaQueryWrapper<ImagingReport>()
                .eq(ImagingReport::getTenantId, tenantId)
                .eq(ImagingReport::getId, id)));
    }

    @Override
    public List<ImagingReport> listByTenantId(Long tenantId) {
        return imagingReportMapper.selectList(new LambdaQueryWrapper<ImagingReport>()
                .eq(ImagingReport::getTenantId, tenantId)
                .orderByDesc(ImagingReport::getId));
    }

    @Override
    public List<ImagingReport> listByPatientId(Long tenantId, Long patientId) {
        return imagingReportMapper.selectList(new LambdaQueryWrapper<ImagingReport>()
                .eq(ImagingReport::getTenantId, tenantId)
                .eq(ImagingReport::getPatientId, patientId)
                .orderByDesc(ImagingReport::getId));
    }

    @Override
    public List<ImagingReport> listByEncounterId(Long tenantId, Long encounterId) {
        return imagingReportMapper.selectList(new LambdaQueryWrapper<ImagingReport>()
                .eq(ImagingReport::getTenantId, tenantId)
                .eq(ImagingReport::getEncounterId, encounterId)
                .orderByDesc(ImagingReport::getId));
    }

    @Override
    public ImagingReport save(ImagingReport imagingReport) {
        imagingReportMapper.insert(imagingReport);
        return imagingReport;
    }

    @Override
    public ImagingReport update(ImagingReport imagingReport) {
        imagingReportMapper.update(imagingReport, new LambdaUpdateWrapper<ImagingReport>()
                .eq(ImagingReport::getTenantId, imagingReport.getTenantId())
                .eq(ImagingReport::getId, imagingReport.getId()));
        return imagingReport;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return imagingReportMapper.delete(new LambdaQueryWrapper<ImagingReport>()
                .eq(ImagingReport::getTenantId, tenantId)
                .eq(ImagingReport::getId, id)) > 0;
    }
}
