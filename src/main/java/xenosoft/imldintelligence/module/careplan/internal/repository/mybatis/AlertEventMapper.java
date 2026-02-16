package xenosoft.imldintelligence.module.careplan.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.careplan.model.AlertEvent;

import java.util.List;

@Mapper
public interface AlertEventMapper {
    AlertEvent findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<AlertEvent> listByTenantId(@Param("tenantId") Long tenantId);

    List<AlertEvent> listByCarePlanId(@Param("tenantId") Long tenantId, @Param("carePlanId") Long carePlanId);

    List<AlertEvent> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    int insert(AlertEvent alertEvent);

    int update(AlertEvent alertEvent);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

