package xenosoft.imldintelligence.module.careplan.internal.repository;

import xenosoft.imldintelligence.module.careplan.model.AlertEvent;

import java.util.List;
import java.util.Optional;

public interface AlertEventRepository {
    Optional<AlertEvent> findById(Long tenantId, Long id);

    List<AlertEvent> listByTenantId(Long tenantId);

    List<AlertEvent> listByCarePlanId(Long tenantId, Long carePlanId);

    List<AlertEvent> listByPatientId(Long tenantId, Long patientId);

    AlertEvent save(AlertEvent alertEvent);

    AlertEvent update(AlertEvent alertEvent);

    Boolean deleteById(Long tenantId, Long id);
}

