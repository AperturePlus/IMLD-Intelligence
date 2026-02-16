package xenosoft.imldintelligence.module.careplan.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.careplan.model.FollowupTask;

import java.util.List;

@Mapper
public interface FollowupTaskMapper {
    FollowupTask findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<FollowupTask> listByTenantId(@Param("tenantId") Long tenantId);

    List<FollowupTask> listByCarePlanId(@Param("tenantId") Long tenantId, @Param("carePlanId") Long carePlanId);

    List<FollowupTask> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    int insert(FollowupTask followupTask);

    int update(FollowupTask followupTask);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

