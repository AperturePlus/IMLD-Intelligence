package xenosoft.imldintelligence.module.community.internal.repository;

public interface CommunityLikeRepository {
    boolean like(Long tenantId, Long postId, Long tocUserId);

    boolean unlike(Long tenantId, Long postId, Long tocUserId);
}

