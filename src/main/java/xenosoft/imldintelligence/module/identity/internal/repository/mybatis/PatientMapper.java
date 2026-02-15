package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;

import java.util.List;

@Mapper
public interface PatientMapper {
    Patient findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    Patient findByPatientNo(@Param("tenantId") Long tenantId, @Param("patientNo") String patientNo);

    List<Patient> listByTenantId(@Param("tenantId") Long tenantId);

    int insert(Patient patient);

    int update(Patient patient);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    int deleteByPatientNo(@Param("tenantId") Long tenantId, @Param("patientNo") String patientNo);
}
