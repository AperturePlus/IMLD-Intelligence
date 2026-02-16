package xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.diagnoses.model.DiagnosisSession;

import java.util.List;

@Mapper
public interface DiagnosisSessionMapper {
    DiagnosisSession findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<DiagnosisSession> listByTenantId(@Param("tenantId") Long tenantId);

    List<DiagnosisSession> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    List<DiagnosisSession> listByEncounterId(@Param("tenantId") Long tenantId, @Param("encounterId") Long encounterId);

    int insert(DiagnosisSession diagnosisSession);

    int update(DiagnosisSession diagnosisSession);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

