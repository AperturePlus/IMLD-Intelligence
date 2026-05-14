package xenosoft.imldintelligence.module.report.internal.repository.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.report.internal.model.Report;
import xenosoft.imldintelligence.module.report.internal.repository.ReportRepository;
import xenosoft.imldintelligence.module.report.internal.repository.mybatis.ReportMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 报告仓储实现类，基于 MyBatis-Plus 完成报告的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {
    private final ReportMapper reportMapper;

    @Override
    public Optional<Report> findById(Long tenantId, Long id) {
        return Optional.ofNullable(reportMapper.selectOne(new LambdaQueryWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .eq(Report::getId, id)));
    }

    @Override
    public Optional<Report> findByReportNo(Long tenantId, String reportNo) {
        return Optional.ofNullable(reportMapper.selectOne(new LambdaQueryWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .eq(Report::getReportNo, reportNo)));
    }

    @Override
    public List<Report> listByTenantId(Long tenantId) {
        return reportMapper.selectList(new LambdaQueryWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .orderByDesc(Report::getId));
    }

    @Override
    public List<Report> listByPatientId(Long tenantId, Long patientId) {
        return reportMapper.selectList(new LambdaQueryWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .eq(Report::getPatientId, patientId)
                .orderByDesc(Report::getId));
    }

    @Override
    public List<Report> listBySessionId(Long tenantId, Long sessionId) {
        return reportMapper.selectList(new LambdaQueryWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .eq(Report::getSessionId, sessionId)
                .orderByDesc(Report::getId));
    }

    @Override
    public Report save(Report report) {
        reportMapper.insert(report);
        return report;
    }

    @Override
    public Report update(Report report) {
        reportMapper.update(report, new LambdaUpdateWrapper<Report>()
                .eq(Report::getTenantId, report.getTenantId())
                .eq(Report::getId, report.getId()));
        return report;
    }

    @Override
    public Optional<Report> lockByIdForUpdate(Long tenantId, Long id) {
        return Optional.ofNullable(reportMapper.selectOne(new LambdaQueryWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .eq(Report::getId, id)
                .last("FOR UPDATE")));
    }

    @Override
    public boolean updateStatusIfCurrent(Long tenantId,
                                         Long id,
                                         String expectedStatus,
                                         String targetStatus,
                                         Long signedBy,
                                         OffsetDateTime signedAt,
                                         JsonNode signatureData) {
        LambdaUpdateWrapper<Report> wrapper = new LambdaUpdateWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .eq(Report::getId, id)
                .eq(Report::getStatus, expectedStatus)
                .set(Report::getStatus, targetStatus)
                .set(Report::getSignedBy, signedBy)
                .set(Report::getSignedAt, signedAt)
                .set(Report::getSignatureData, signatureData)
                .set(Report::getUpdatedAt, OffsetDateTime.now().withNano(0));
        return reportMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean updateCurrentVersionIfMatches(Long tenantId,
                                                 Long id,
                                                 Integer expectedVersion,
                                                 Integer targetVersion,
                                                 OffsetDateTime updatedAt) {
        LambdaUpdateWrapper<Report> wrapper = new LambdaUpdateWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .eq(Report::getId, id)
                .eq(Report::getCurrentVersion, expectedVersion)
                .set(Report::getCurrentVersion, targetVersion)
                .set(Report::getUpdatedAt, updatedAt);
        return reportMapper.update(null, wrapper) > 0;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return reportMapper.delete(new LambdaQueryWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .eq(Report::getId, id)) > 0;
    }
}
