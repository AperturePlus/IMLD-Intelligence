package xenosoft.imldintelligence.module.careplan.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.careplan.model.CarePlan;

import java.util.List;

@Mapper
public interface CarePlanMapper {
    CarePlan findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<CarePlan> listByTenantId(@Param("tenantId") Long tenantId);

    List<CarePlan> listByPatientId(@Param("tenantId") Long tenantId, @Param("patientId") Long patientId);

    int insert(CarePlan carePlan);

    int update(CarePlan carePlan);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

