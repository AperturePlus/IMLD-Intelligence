package xenosoft.imldintelligence.module.report.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.report.model.ReportVersion;

import java.util.List;

@Mapper
public interface ReportVersionMapper {
    ReportVersion findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    ReportVersion findByReportIdAndVersionNum(@Param("tenantId") Long tenantId,
                                              @Param("reportId") Long reportId,
                                              @Param("versionNum") Integer versionNum);

    List<ReportVersion> listByReportId(@Param("tenantId") Long tenantId, @Param("reportId") Long reportId);

    int insert(ReportVersion reportVersion);

    int update(ReportVersion reportVersion);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

