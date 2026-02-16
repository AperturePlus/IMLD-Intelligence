package xenosoft.imldintelligence.module.clinical.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.clinical.internal.model.ImagingReport;

import java.util.List;

@Mapper
public interface ImagingReportMapper {
    ImagingReport findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<ImagingReport> listByTenantId(@Param("tenantId") Long tenantId);

    List<ImagingReport> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    List<ImagingReport> listByEncounterId(@Param("tenantId") Long tenantId, @Param("encounterId") Long encounterId);

    int insert(ImagingReport imagingReport);

    int update(ImagingReport imagingReport);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

