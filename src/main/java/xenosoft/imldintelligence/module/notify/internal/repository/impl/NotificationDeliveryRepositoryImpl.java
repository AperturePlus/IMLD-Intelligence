package xenosoft.imldintelligence.module.notify.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.notify.internal.model.NotificationDelivery;
import xenosoft.imldintelligence.module.notify.internal.repository.NotificationDeliveryRepository;
import xenosoft.imldintelligence.module.notify.internal.repository.mybatis.NotificationDeliveryMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationDeliveryRepositoryImpl implements NotificationDeliveryRepository {
    private final NotificationDeliveryMapper notificationDeliveryMapper;

    @Override
    public Optional<NotificationDelivery> findById(Long tenantId, Long id) {
        return Optional.ofNullable(notificationDeliveryMapper.findById(tenantId, id));
    }

    @Override
    public List<NotificationDelivery> listByTenantId(Long tenantId) {
        return notificationDeliveryMapper.listByTenantId(tenantId);
    }

    @Override
    public List<NotificationDelivery> listByMessageId(Long tenantId, Long messageId) {
        return notificationDeliveryMapper.listByMessageId(tenantId, messageId);
    }

    @Override
    public NotificationDelivery save(NotificationDelivery notificationDelivery) {
        notificationDeliveryMapper.insert(notificationDelivery);
        return notificationDelivery;
    }

    @Override
    public NotificationDelivery update(NotificationDelivery notificationDelivery) {
        notificationDeliveryMapper.update(notificationDelivery);
        return notificationDelivery;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return notificationDeliveryMapper.deleteById(tenantId, id) > 0;
    }
}

