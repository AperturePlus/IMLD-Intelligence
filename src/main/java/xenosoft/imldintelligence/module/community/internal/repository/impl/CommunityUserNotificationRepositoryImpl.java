package xenosoft.imldintelligence.module.community.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.community.internal.model.CommunityUserNotification;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityUserNotificationRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityUserNotificationMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommunityUserNotificationRepositoryImpl implements CommunityUserNotificationRepository {
    private final CommunityUserNotificationMapper communityUserNotificationMapper;

    @Override
    public Optional<CommunityUserNotification> findById(Long tenantId, Long id) {
        return Optional.ofNullable(communityUserNotificationMapper.selectOne(new LambdaQueryWrapper<CommunityUserNotification>()
                .eq(CommunityUserNotification::getTenantId, tenantId)
                .eq(CommunityUserNotification::getId, id)));
    }

    @Override
    public List<CommunityUserNotification> listByUserId(Long tenantId, Long tocUserId, Boolean isRead, long offset, int limit) {
        LambdaQueryWrapper<CommunityUserNotification> query = new LambdaQueryWrapper<CommunityUserNotification>()
                .eq(CommunityUserNotification::getTenantId, tenantId)
                .eq(CommunityUserNotification::getTocUserId, tocUserId);
        if (isRead != null) {
            query.eq(CommunityUserNotification::getIsRead, isRead);
        }
        query.orderByDesc(CommunityUserNotification::getCreatedAt);
        query.last("LIMIT " + limit + " OFFSET " + offset);
        return communityUserNotificationMapper.selectList(query);
    }

    @Override
    public long countUnreadByUserId(Long tenantId, Long tocUserId) {
        return communityUserNotificationMapper.selectCount(new LambdaQueryWrapper<CommunityUserNotification>()
                .eq(CommunityUserNotification::getTenantId, tenantId)
                .eq(CommunityUserNotification::getTocUserId, tocUserId)
                .eq(CommunityUserNotification::getIsRead, false));
    }

    @Override
    public CommunityUserNotification save(CommunityUserNotification notification) {
        communityUserNotificationMapper.insert(notification);
        return notification;
    }

    @Override
    public boolean updateReadStatus(Long tenantId, Long id, Boolean isRead) {
        int rows = communityUserNotificationMapper.update(null, new LambdaUpdateWrapper<CommunityUserNotification>()
                .eq(CommunityUserNotification::getTenantId, tenantId)
                .eq(CommunityUserNotification::getId, id)
                .set(CommunityUserNotification::getIsRead, isRead));
        return rows > 0;
    }
}
