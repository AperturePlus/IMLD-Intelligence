package xenosoft.imldintelligence.module.report.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.report.model.ReportTemplate;

import java.util.List;

@Mapper
public interface ReportTemplateMapper {
    ReportTemplate findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    ReportTemplate findByTemplateCodeAndVersionNo(@Param("tenantId") Long tenantId,
                                                  @Param("templateCode") String templateCode,
                                                  @Param("versionNo") Integer versionNo);

    List<ReportTemplate> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(ReportTemplate reportTemplate);

    int update(ReportTemplate reportTemplate);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

