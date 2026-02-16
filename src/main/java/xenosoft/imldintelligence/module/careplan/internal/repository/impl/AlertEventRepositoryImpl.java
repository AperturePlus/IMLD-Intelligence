package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.repository.AlertEventRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.AlertEventMapper;
import xenosoft.imldintelligence.module.careplan.model.AlertEvent;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlertEventRepositoryImpl implements AlertEventRepository {
    private final AlertEventMapper alertEventMapper;

    @Override
    public Optional<AlertEvent> findById(Long tenantId, Long id) {
        return Optional.ofNullable(alertEventMapper.findById(tenantId, id));
    }

    @Override
    public List<AlertEvent> listByTenantId(Long tenantId) {
        return alertEventMapper.listByTenantId(tenantId);
    }

    @Override
    public List<AlertEvent> listByCarePlanId(Long tenantId, Long carePlanId) {
        return alertEventMapper.listByCarePlanId(tenantId, carePlanId);
    }

    @Override
    public List<AlertEvent> listByPatientId(Long tenantId, Long patientId) {
        return alertEventMapper.listByPatientId(tenantId, patientId);
    }

    @Override
    public AlertEvent save(AlertEvent alertEvent) {
        alertEventMapper.insert(alertEvent);
        return alertEvent;
    }

    @Override
    public AlertEvent update(AlertEvent alertEvent) {
        alertEventMapper.update(alertEvent);
        return alertEvent;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return alertEventMapper.deleteById(tenantId, id) > 0;
    }
}

