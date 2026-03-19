package xenosoft.imldintelligence.module.report.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.report.internal.model.Report;
import xenosoft.imldintelligence.module.report.internal.repository.ReportRepository;
import xenosoft.imldintelligence.module.report.internal.repository.mybatis.ReportMapper;

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
    public Boolean deleteById(Long tenantId, Long id) {
        return reportMapper.delete(new LambdaQueryWrapper<Report>()
                .eq(Report::getTenantId, tenantId)
                .eq(Report::getId, id)) > 0;
    }
}
