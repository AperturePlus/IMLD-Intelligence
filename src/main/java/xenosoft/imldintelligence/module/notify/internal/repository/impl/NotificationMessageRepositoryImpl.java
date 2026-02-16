package xenosoft.imldintelligence.module.notify.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.notify.internal.model.NotificationMessage;
import xenosoft.imldintelligence.module.notify.internal.repository.NotificationMessageRepository;
import xenosoft.imldintelligence.module.notify.internal.repository.mybatis.NotificationMessageMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationMessageRepositoryImpl implements NotificationMessageRepository {
    private final NotificationMessageMapper notificationMessageMapper;

    @Override
    public Optional<NotificationMessage> findById(Long tenantId, Long id) {
        return Optional.ofNullable(notificationMessageMapper.findById(tenantId, id));
    }

    @Override
    public List<NotificationMessage> listByTenantId(Long tenantId) {
        return notificationMessageMapper.listByTenantId(tenantId);
    }

    @Override
    public List<NotificationMessage> listByReceiver(Long tenantId, String receiverType, Long receiverRefId) {
        return notificationMessageMapper.listByReceiver(tenantId, receiverType, receiverRefId);
    }

    @Override
    public NotificationMessage save(NotificationMessage notificationMessage) {
        notificationMessageMapper.insert(notificationMessage);
        return notificationMessage;
    }

    @Override
    public NotificationMessage update(NotificationMessage notificationMessage) {
        notificationMessageMapper.update(notificationMessage);
        return notificationMessage;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return notificationMessageMapper.deleteById(tenantId, id) > 0;
    }
}

