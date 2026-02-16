package xenosoft.imldintelligence.module.report.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.report.internal.repository.ReportVersionRepository;
import xenosoft.imldintelligence.module.report.internal.repository.mybatis.ReportVersionMapper;
import xenosoft.imldintelligence.module.report.model.ReportVersion;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReportVersionRepositoryImpl implements ReportVersionRepository {
    private final ReportVersionMapper reportVersionMapper;

    @Override
    public Optional<ReportVersion> findById(Long tenantId, Long id) {
        return Optional.ofNullable(reportVersionMapper.findById(tenantId, id));
    }

    @Override
    public Optional<ReportVersion> findByReportIdAndVersionNum(Long tenantId, Long reportId, Integer versionNum) {
        return Optional.ofNullable(reportVersionMapper.findByReportIdAndVersionNum(tenantId, reportId, versionNum));
    }

    @Override
    public List<ReportVersion> listByReportId(Long tenantId, Long reportId) {
        return reportVersionMapper.listByReportId(tenantId, reportId);
    }

    @Override
    public ReportVersion save(ReportVersion reportVersion) {
        reportVersionMapper.insert(reportVersion);
        return reportVersion;
    }

    @Override
    public ReportVersion update(ReportVersion reportVersion) {
        reportVersionMapper.update(reportVersion);
        return reportVersion;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return reportVersionMapper.deleteById(tenantId, id) > 0;
    }
}

