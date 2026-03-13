package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;

import java.util.List;

/**
 * 患者 MyBatis Mapper，定义患者的数据读写映射。
 */
@Mapper
public interface PatientMapper {
    Patient findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    Patient findByPatientNo(@Param("tenantId") Long tenantId, @Param("patientNo") String patientNo);

    List<Patient> listByTenantId(@Param("tenantId") Long tenantId);

    long countByCondition(@Param("tenantId") Long tenantId,
                          @Param("patientNo") String patientNo,
                          @Param("patientNameKeyword") String patientNameKeyword,
                          @Param("patientType") String patientType,
                          @Param("status") String status);

    List<Patient> listByCondition(@Param("tenantId") Long tenantId,
                                   @Param("patientNo") String patientNo,
                                   @Param("patientNameKeyword") String patientNameKeyword,
                                   @Param("patientType") String patientType,
                                   @Param("status") String status,
                                   @Param("offset") long offset,
                                   @Param("limit") int limit);

    int insert(Patient patient);

    int update(Patient patient);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    int deleteByPatientNo(@Param("tenantId") Long tenantId, @Param("patientNo") String patientNo);
}
