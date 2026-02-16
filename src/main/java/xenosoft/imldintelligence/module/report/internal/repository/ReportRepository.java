package xenosoft.imldintelligence.module.report.internal.repository;

import xenosoft.imldintelligence.module.report.model.Report;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {
    Optional<Report> findById(Long tenantId, Long id);

    Optional<Report> findByReportNo(Long tenantId, String reportNo);

    List<Report> listByTenantId(Long tenantId);

    List<Report> listByPatientId(Long tenantId, Long patientId);

    List<Report> listBySessionId(Long tenantId, Long sessionId);

    Report save(Report report);

    Report update(Report report);

    Boolean deleteById(Long tenantId, Long id);
}

