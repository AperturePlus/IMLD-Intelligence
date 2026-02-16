package xenosoft.imldintelligence.module.notify.internal.repository;

import xenosoft.imldintelligence.module.notify.internal.model.NotificationMessage;

import java.util.List;
import java.util.Optional;

public interface NotificationMessageRepository {
    Optional<NotificationMessage> findById(Long tenantId, Long id);

    List<NotificationMessage> listByTenantId(Long tenantId);

    List<NotificationMessage> listByReceiver(Long tenantId, String receiverType, Long receiverRefId);

    NotificationMessage save(NotificationMessage notificationMessage);

    NotificationMessage update(NotificationMessage notificationMessage);

    Boolean deleteById(Long tenantId, Long id);
}

