package xenosoft.imldintelligence.module.report.internal.repository;

import xenosoft.imldintelligence.module.report.model.ReportTemplate;

import java.util.List;
import java.util.Optional;

public interface ReportTemplateRepository {
    Optional<ReportTemplate> findById(Long tenantId, Long id);

    Optional<ReportTemplate> findByTemplateCodeAndVersionNo(Long tenantId, String templateCode, Integer versionNo);

    List<ReportTemplate> listByTenantId(Long tenantId);

    ReportTemplate save(ReportTemplate reportTemplate);

    ReportTemplate update(ReportTemplate reportTemplate);

    Boolean deleteById(Long tenantId, Long id);
}

