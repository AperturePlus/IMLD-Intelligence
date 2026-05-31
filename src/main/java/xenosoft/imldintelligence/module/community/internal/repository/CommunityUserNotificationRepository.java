package xenosoft.imldintelligence.module.community.internal.repository;

import xenosoft.imldintelligence.module.community.internal.model.CommunityUserNotification;

import java.util.List;
import java.util.Optional;

public interface CommunityUserNotificationRepository {
    Optional<CommunityUserNotification> findById(Long tenantId, Long id);

    List<CommunityUserNotification> listByUserId(Long tenantId, Long tocUserId, Boolean isRead, long offset, int limit);

    long countUnreadByUserId(Long tenantId, Long tocUserId);

    CommunityUserNotification save(CommunityUserNotification notification);

    boolean updateReadStatus(Long tenantId, Long id, Boolean isRead);
}
