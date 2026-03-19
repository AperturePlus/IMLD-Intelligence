package xenosoft.imldintelligence.module.careplan.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.careplan.internal.model.AlertAction;
import xenosoft.imldintelligence.module.careplan.internal.repository.AlertActionRepository;
import xenosoft.imldintelligence.module.careplan.internal.repository.mybatis.AlertActionMapper;

import java.util.List;
import java.util.Optional;

/**
 * 预警动作仓储实现类，基于 MyBatis-Plus 完成预警动作的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class AlertActionRepositoryImpl implements AlertActionRepository {
    private final AlertActionMapper alertActionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AlertAction> findById(Long tenantId, Long id) {
        return Optional.ofNullable(alertActionMapper.selectOne(new LambdaQueryWrapper<AlertAction>()
                .eq(AlertAction::getTenantId, tenantId)
                .eq(AlertAction::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AlertAction> listByTenantId(Long tenantId) {
        return alertActionMapper.selectList(new LambdaQueryWrapper<AlertAction>()
                .eq(AlertAction::getTenantId, tenantId)
                .orderByDesc(AlertAction::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AlertAction> listByAlertId(Long tenantId, Long alertId) {
        return alertActionMapper.selectList(new LambdaQueryWrapper<AlertAction>()
                .eq(AlertAction::getTenantId, tenantId)
                .eq(AlertAction::getAlertId, alertId)
                .orderByDesc(AlertAction::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertAction save(AlertAction alertAction) {
        alertActionMapper.insert(alertAction);
        return alertAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertAction update(AlertAction alertAction) {
        alertActionMapper.update(alertAction, new LambdaUpdateWrapper<AlertAction>()
                .eq(AlertAction::getTenantId, alertAction.getTenantId())
                .eq(AlertAction::getId, alertAction.getId()));
        return alertAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return alertActionMapper.delete(new LambdaQueryWrapper<AlertAction>()
                .eq(AlertAction::getTenantId, tenantId)
                .eq(AlertAction::getId, id)) > 0;
    }
}
