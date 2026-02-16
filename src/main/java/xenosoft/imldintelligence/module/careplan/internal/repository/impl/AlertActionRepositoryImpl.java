package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.repository.AlertActionRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.AlertActionMapper;
import xenosoft.imldintelligence.module.careplan.model.AlertAction;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlertActionRepositoryImpl implements AlertActionRepository {
    private final AlertActionMapper alertActionMapper;

    @Override
    public Optional<AlertAction> findById(Long tenantId, Long id) {
        return Optional.ofNullable(alertActionMapper.findById(tenantId, id));
    }

    @Override
    public List<AlertAction> listByTenantId(Long tenantId) {
        return alertActionMapper.listByTenantId(tenantId);
    }

    @Override
    public List<AlertAction> listByAlertId(Long tenantId, Long alertId) {
        return alertActionMapper.listByAlertId(tenantId, alertId);
    }

    @Override
    public AlertAction save(AlertAction alertAction) {
        alertActionMapper.insert(alertAction);
        return alertAction;
    }

    @Override
    public AlertAction update(AlertAction alertAction) {
        alertActionMapper.update(alertAction);
        return alertAction;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return alertActionMapper.deleteById(tenantId, id) > 0;
    }
}

