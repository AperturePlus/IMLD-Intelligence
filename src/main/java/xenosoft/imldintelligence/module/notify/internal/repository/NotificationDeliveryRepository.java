package xenosoft.imldintelligence.module.notify.internal.repository;

import xenosoft.imldintelligence.module.notify.internal.model.NotificationDelivery;

import java.util.List;
import java.util.Optional;

public interface NotificationDeliveryRepository {
    Optional<NotificationDelivery> findById(Long tenantId, Long id);

    List<NotificationDelivery> listByTenantId(Long tenantId);

    List<NotificationDelivery> listByMessageId(Long tenantId, Long messageId);

    NotificationDelivery save(NotificationDelivery notificationDelivery);

    NotificationDelivery update(NotificationDelivery notificationDelivery);

    Boolean deleteById(Long tenantId, Long id);
}

