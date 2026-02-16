package xenosoft.imldintelligence.module.careplan.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.careplan.model.PatientReportedData;

import java.util.List;

@Mapper
public interface PatientReportedDataMapper {
    PatientReportedData findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<PatientReportedData> listByTenantId(@Param("tenantId") Long tenantId);

    List<PatientReportedData> listByCarePlanId(@Param("tenantId") Long tenantId, @Param("carePlanId") Long carePlanId);

    List<PatientReportedData> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    int insert(PatientReportedData patientReportedData);

    int update(PatientReportedData patientReportedData);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

