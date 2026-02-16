package xenosoft.imldintelligence.module.careplan.internal.repository;

import xenosoft.imldintelligence.module.careplan.model.AlertAction;

import java.util.List;
import java.util.Optional;

public interface AlertActionRepository {
    Optional<AlertAction> findById(Long tenantId, Long id);

    List<AlertAction> listByTenantId(Long tenantId);

    List<AlertAction> listByAlertId(Long tenantId, Long alertId);

    AlertAction save(AlertAction alertAction);

    AlertAction update(AlertAction alertAction);

    Boolean deleteById(Long tenantId, Long id);
}

