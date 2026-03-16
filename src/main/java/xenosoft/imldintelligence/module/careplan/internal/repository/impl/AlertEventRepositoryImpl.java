package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.model.AlertEvent;
import xenosoft.imldintelligence.module.careplan.internal.repository.AlertEventRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.AlertEventMapper;

import java.util.List;
import java.util.Optional;

/**
 * 预警事件仓储实现类，基于 MyBatis-Plus 完成预警事件的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class AlertEventRepositoryImpl implements AlertEventRepository {
    private final AlertEventMapper alertEventMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AlertEvent> findById(Long tenantId, Long id) {
        return Optional.ofNullable(alertEventMapper.selectOne(new LambdaQueryWrapper<AlertEvent>()
                .eq(AlertEvent::getTenantId, tenantId)
                .eq(AlertEvent::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AlertEvent> listByTenantId(Long tenantId) {
        return alertEventMapper.selectList(new LambdaQueryWrapper<AlertEvent>()
                .eq(AlertEvent::getTenantId, tenantId)
                .orderByDesc(AlertEvent::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AlertEvent> listByCarePlanId(Long tenantId, Long carePlanId) {
        return alertEventMapper.selectList(new LambdaQueryWrapper<AlertEvent>()
                .eq(AlertEvent::getTenantId, tenantId)
                .eq(AlertEvent::getCarePlanId, carePlanId)
                .orderByDesc(AlertEvent::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AlertEvent> listByPatientId(Long tenantId, Long patientId) {
        return alertEventMapper.selectList(new LambdaQueryWrapper<AlertEvent>()
                .eq(AlertEvent::getTenantId, tenantId)
                .eq(AlertEvent::getPatientId, patientId)
                .orderByDesc(AlertEvent::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertEvent save(AlertEvent alertEvent) {
        alertEventMapper.insert(alertEvent);
        return alertEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertEvent update(AlertEvent alertEvent) {
        alertEventMapper.update(alertEvent, new LambdaUpdateWrapper<AlertEvent>()
                .eq(AlertEvent::getTenantId, alertEvent.getTenantId())
                .eq(AlertEvent::getId, alertEvent.getId()));
        return alertEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return alertEventMapper.delete(new LambdaQueryWrapper<AlertEvent>()
                .eq(AlertEvent::getTenantId, tenantId)
                .eq(AlertEvent::getId, id)) > 0;
    }
}
