package xenosoft.imldintelligence.module.report.internal.repository;

import xenosoft.imldintelligence.module.report.model.ReportVersion;

import java.util.List;
import java.util.Optional;

public interface ReportVersionRepository {
    Optional<ReportVersion> findById(Long tenantId, Long id);

    Optional<ReportVersion> findByReportIdAndVersionNum(Long tenantId, Long reportId, Integer versionNum);

    List<ReportVersion> listByReportId(Long tenantId, Long reportId);

    ReportVersion save(ReportVersion reportVersion);

    ReportVersion update(ReportVersion reportVersion);

    Boolean deleteById(Long tenantId, Long id);
}

