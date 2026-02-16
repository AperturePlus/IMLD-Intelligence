package xenosoft.imldintelligence.module.clinical.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.clinical.internal.model.LabResult;

import java.util.List;

@Mapper
public interface LabResultMapper {
    LabResult findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<LabResult> listByTenantId(@Param("tenantId") Long tenantId);

    List<LabResult> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    List<LabResult> listByEncounterId(@Param("tenantId") Long tenantId, @Param("encounterId") Long encounterId);

    int insert(LabResult labResult);

    int update(LabResult labResult);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

