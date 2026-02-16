package xenosoft.imldintelligence.module.report.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.report.internal.repository.ReportRepository;
import xenosoft.imldintelligence.module.report.internal.repository.mybatis.ReportMapper;
import xenosoft.imldintelligence.module.report.model.Report;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {
    private final ReportMapper reportMapper;

    @Override
    public Optional<Report> findById(Long tenantId, Long id) {
        return Optional.ofNullable(reportMapper.findById(tenantId, id));
    }

    @Override
    public Optional<Report> findByReportNo(Long tenantId, String reportNo) {
        return Optional.ofNullable(reportMapper.findByReportNo(tenantId, reportNo));
    }

    @Override
    public List<Report> listByTenantId(Long tenantId) {
        return reportMapper.listByTenantId(tenantId);
    }

    @Override
    public List<Report> listByPatientId(Long tenantId, Long patientId) {
        return reportMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public List<Report> listBySessionId(Long tenantId, Long sessionId) {
        return reportMapper.listBySessionId(tenantId, sessionId);
    }

    @Override
    public Report save(Report report) {
        reportMapper.insert(report);
        return report;
    }

    @Override
    public Report update(Report report) {
        reportMapper.update(report);
        return report;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return reportMapper.deleteById(tenantId, id) > 0;
    }
}

