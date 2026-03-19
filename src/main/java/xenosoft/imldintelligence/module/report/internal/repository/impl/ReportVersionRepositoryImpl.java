package xenosoft.imldintelligence.module.report.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.report.internal.model.ReportVersion;
import xenosoft.imldintelligence.module.report.internal.repository.ReportVersionRepository;
import xenosoft.imldintelligence.module.report.internal.repository.mybatis.ReportVersionMapper;

import java.util.List;
import java.util.Optional;

/**
 * 报告版本仓储实现类，基于 MyBatis-Plus 完成报告版本的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class ReportVersionRepositoryImpl implements ReportVersionRepository {
    private final ReportVersionMapper reportVersionMapper;

    @Override
    public Optional<ReportVersion> findById(Long tenantId, Long id) {
        return Optional.ofNullable(reportVersionMapper.selectOne(new LambdaQueryWrapper<ReportVersion>()
                .eq(ReportVersion::getTenantId, tenantId)
                .eq(ReportVersion::getId, id)));
    }

    @Override
    public Optional<ReportVersion> findByReportIdAndVersionNum(Long tenantId, Long reportId, Integer versionNum) {
        return Optional.ofNullable(reportVersionMapper.selectOne(new LambdaQueryWrapper<ReportVersion>()
                .eq(ReportVersion::getTenantId, tenantId)
                .eq(ReportVersion::getReportId, reportId)
                .eq(ReportVersion::getVersionNum, versionNum)));
    }

    @Override
    public List<ReportVersion> listByReportId(Long tenantId, Long reportId) {
        return reportVersionMapper.selectList(new LambdaQueryWrapper<ReportVersion>()
                .eq(ReportVersion::getTenantId, tenantId)
                .eq(ReportVersion::getReportId, reportId)
                .orderByDesc(ReportVersion::getVersionNum));
    }

    @Override
    public ReportVersion save(ReportVersion reportVersion) {
        reportVersionMapper.insert(reportVersion);
        return reportVersion;
    }

    @Override
    public ReportVersion update(ReportVersion reportVersion) {
        reportVersionMapper.update(reportVersion, new LambdaUpdateWrapper<ReportVersion>()
                .eq(ReportVersion::getTenantId, reportVersion.getTenantId())
                .eq(ReportVersion::getId, reportVersion.getId()));
        return reportVersion;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return reportVersionMapper.delete(new LambdaQueryWrapper<ReportVersion>()
                .eq(ReportVersion::getTenantId, tenantId)
                .eq(ReportVersion::getId, id)) > 0;
    }
}
