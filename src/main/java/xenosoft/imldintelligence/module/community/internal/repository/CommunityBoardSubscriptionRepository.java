package xenosoft.imldintelligence.module.community.internal.repository;

import xenosoft.imldintelligence.module.community.internal.model.CommunityBoardSubscription;

import java.util.List;
import java.util.Optional;

public interface CommunityBoardSubscriptionRepository {
    Optional<CommunityBoardSubscription> findById(Long tenantId, Long id);

    List<CommunityBoardSubscription> listByUserId(Long tenantId, Long tocUserId);

    List<CommunityBoardSubscription> listByBoardId(Long tenantId, Long boardId);

    CommunityBoardSubscription save(CommunityBoardSubscription sub);

    boolean deleteByUserIdAndBoardId(Long tenantId, Long tocUserId, Long boardId);
}
