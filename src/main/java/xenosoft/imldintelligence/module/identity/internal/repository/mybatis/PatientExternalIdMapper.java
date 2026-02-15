package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.PatientExternalId;

import java.util.List;

@Mapper
public interface PatientExternalIdMapper {
    PatientExternalId findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    PatientExternalId findByIdTypeAndIdValue(@Param("tenantId") Long tenantId, @Param("idType") String idType, @Param("idValue") String idValue);

    List<PatientExternalId> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    List<PatientExternalId> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(PatientExternalId patientExternalId);

    int update(PatientExternalId patientExternalId);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
