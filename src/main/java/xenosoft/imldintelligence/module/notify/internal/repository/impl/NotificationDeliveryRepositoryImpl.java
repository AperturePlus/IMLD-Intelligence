package xenosoft.imldintelligence.module.notify.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.notify.internal.model.NotificationDelivery;
import xenosoft.imldintelligence.module.notify.internal.repository.NotificationDeliveryRepository;
import xenosoft.imldintelligence.module.notify.internal.repository.mybatis.NotificationDeliveryMapper;

import java.util.List;
import java.util.Optional;

/**
 * 通知投递记录仓储实现类，基于 MyBatis-Plus 完成通知投递记录的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class NotificationDeliveryRepositoryImpl implements NotificationDeliveryRepository {
    private final NotificationDeliveryMapper notificationDeliveryMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<NotificationDelivery> findById(Long tenantId, Long id) {
        return Optional.ofNullable(notificationDeliveryMapper.selectOne(new LambdaQueryWrapper<NotificationDelivery>()
                .eq(NotificationDelivery::getTenantId, tenantId)
                .eq(NotificationDelivery::getId, id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotificationDelivery> listByTenantId(Long tenantId) {
        return notificationDeliveryMapper.selectList(new LambdaQueryWrapper<NotificationDelivery>()
                .eq(NotificationDelivery::getTenantId, tenantId)
                .orderByDesc(NotificationDelivery::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotificationDelivery> listByMessageId(Long tenantId, Long messageId) {
        return notificationDeliveryMapper.selectList(new LambdaQueryWrapper<NotificationDelivery>()
                .eq(NotificationDelivery::getTenantId, tenantId)
                .eq(NotificationDelivery::getMessageId, messageId)
                .orderByDesc(NotificationDelivery::getId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationDelivery save(NotificationDelivery notificationDelivery) {
        notificationDeliveryMapper.insert(notificationDelivery);
        return notificationDelivery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationDelivery update(NotificationDelivery notificationDelivery) {
        notificationDeliveryMapper.update(notificationDelivery, new LambdaUpdateWrapper<NotificationDelivery>()
                .eq(NotificationDelivery::getTenantId, notificationDelivery.getTenantId())
                .eq(NotificationDelivery::getId, notificationDelivery.getId()));
        return notificationDelivery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return notificationDeliveryMapper.delete(new LambdaQueryWrapper<NotificationDelivery>()
                .eq(NotificationDelivery::getTenantId, tenantId)
                .eq(NotificationDelivery::getId, id)) > 0;
    }
}
