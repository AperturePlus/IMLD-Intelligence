package xenosoft.imldintelligence.module.diagnoses.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.diagnoses.model.DoctorFeedback;

import java.util.List;

@Mapper
public interface DoctorFeedbackMapper {
    DoctorFeedback findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<DoctorFeedback> listByTenantId(@Param("tenantId") Long tenantId);

    List<DoctorFeedback> listBySessionId(@Param("tenantId") Long tenantId, @Param("sessionId") Long sessionId);

    List<DoctorFeedback> listByResultId(@Param("tenantId") Long tenantId, @Param("resultId") Long resultId);

    int insert(DoctorFeedback doctorFeedback);

    int update(DoctorFeedback doctorFeedback);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

