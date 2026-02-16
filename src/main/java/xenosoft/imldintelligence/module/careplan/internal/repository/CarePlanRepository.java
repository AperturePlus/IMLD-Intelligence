package xenosoft.imldintelligence.module.careplan.internal.repository;

import xenosoft.imldintelligence.module.careplan.model.CarePlan;

import java.util.List;
import java.util.Optional;

public interface CarePlanRepository {
    Optional<CarePlan> findById(Long tenantId, Long id);

    List<CarePlan> listByTenantId(Long tenantId);

    List<CarePlan> listByPatientId(Long tenantId, Long patientId);

    CarePlan save(CarePlan carePlan);

    CarePlan update(CarePlan carePlan);

    Boolean deleteById(Long tenantId, Long id);
}

