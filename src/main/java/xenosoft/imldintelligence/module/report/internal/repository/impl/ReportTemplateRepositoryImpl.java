package xenosoft.imldintelligence.module.report.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.report.internal.model.ReportTemplate;
import xenosoft.imldintelligence.module.report.internal.repository.ReportTemplateRepository;
import xenosoft.imldintelligence.module.report.internal.repository.mybatis.ReportTemplateMapper;

import java.util.List;
import java.util.Optional;

/**
 * 报告模板仓储实现类，基于 MyBatis-Plus 完成报告模板的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class ReportTemplateRepositoryImpl implements ReportTemplateRepository {
    private final ReportTemplateMapper reportTemplateMapper;

    @Override
    public Optional<ReportTemplate> findById(Long tenantId, Long id) {
        return Optional.ofNullable(reportTemplateMapper.selectOne(new LambdaQueryWrapper<ReportTemplate>()
                .eq(ReportTemplate::getTenantId, tenantId)
                .eq(ReportTemplate::getId, id)));
    }

    @Override
    public Optional<ReportTemplate> findByTemplateCodeAndVersionNo(Long tenantId, String templateCode, Integer versionNo) {
        return Optional.ofNullable(reportTemplateMapper.selectOne(new LambdaQueryWrapper<ReportTemplate>()
                .eq(ReportTemplate::getTenantId, tenantId)
                .eq(ReportTemplate::getTemplateCode, templateCode)
                .eq(ReportTemplate::getVersionNo, versionNo)));
    }

    @Override
    public List<ReportTemplate> listByTenantId(Long tenantId) {
        return reportTemplateMapper.selectList(new LambdaQueryWrapper<ReportTemplate>()
                .eq(ReportTemplate::getTenantId, tenantId)
                .orderByDesc(ReportTemplate::getId));
    }

    @Override
    public ReportTemplate save(ReportTemplate reportTemplate) {
        reportTemplateMapper.insert(reportTemplate);
        return reportTemplate;
    }

    @Override
    public ReportTemplate update(ReportTemplate reportTemplate) {
        reportTemplateMapper.update(reportTemplate, new LambdaUpdateWrapper<ReportTemplate>()
                .eq(ReportTemplate::getTenantId, reportTemplate.getTenantId())
                .eq(ReportTemplate::getId, reportTemplate.getId()));
        return reportTemplate;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return reportTemplateMapper.update(null, new LambdaUpdateWrapper<ReportTemplate>()
                .eq(ReportTemplate::getTenantId, tenantId)
                .eq(ReportTemplate::getId, id)
                .set(ReportTemplate::getStatus, "INACTIVE")) > 0;
    }
}
