package xenosoft.imldintelligence.module.community.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.community.internal.model.CommunityBoardSubscription;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityBoardSubscriptionRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityBoardSubscriptionMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommunityBoardSubscriptionRepositoryImpl implements CommunityBoardSubscriptionRepository {
    private final CommunityBoardSubscriptionMapper communityBoardSubscriptionMapper;

    @Override
    public Optional<CommunityBoardSubscription> findById(Long tenantId, Long id) {
        return Optional.ofNullable(communityBoardSubscriptionMapper.selectOne(new LambdaQueryWrapper<CommunityBoardSubscription>()
                .eq(CommunityBoardSubscription::getTenantId, tenantId)
                .eq(CommunityBoardSubscription::getId, id)));
    }

    @Override
    public List<CommunityBoardSubscription> listByUserId(Long tenantId, Long tocUserId) {
        return communityBoardSubscriptionMapper.selectList(new LambdaQueryWrapper<CommunityBoardSubscription>()
                .eq(CommunityBoardSubscription::getTenantId, tenantId)
                .eq(CommunityBoardSubscription::getTocUserId, tocUserId)
                .orderByDesc(CommunityBoardSubscription::getCreatedAt));
    }

    @Override
    public List<CommunityBoardSubscription> listByBoardId(Long tenantId, Long boardId) {
        return communityBoardSubscriptionMapper.selectList(new LambdaQueryWrapper<CommunityBoardSubscription>()
                .eq(CommunityBoardSubscription::getTenantId, tenantId)
                .eq(CommunityBoardSubscription::getBoardId, boardId)
                .orderByDesc(CommunityBoardSubscription::getCreatedAt));
    }

    @Override
    public CommunityBoardSubscription save(CommunityBoardSubscription sub) {
        communityBoardSubscriptionMapper.insert(sub);
        return sub;
    }

    @Override
    public boolean deleteByUserIdAndBoardId(Long tenantId, Long tocUserId, Long boardId) {
        int rows = communityBoardSubscriptionMapper.delete(new LambdaQueryWrapper<CommunityBoardSubscription>()
                .eq(CommunityBoardSubscription::getTenantId, tenantId)
                .eq(CommunityBoardSubscription::getTocUserId, tocUserId)
                .eq(CommunityBoardSubscription::getBoardId, boardId));
        return rows > 0;
    }
}
