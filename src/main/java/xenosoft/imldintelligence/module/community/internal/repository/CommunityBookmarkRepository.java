package xenosoft.imldintelligence.module.community.internal.repository;

public interface CommunityBookmarkRepository {
    boolean bookmark(Long tenantId, Long postId, Long tocUserId);

    boolean unbookmark(Long tenantId, Long postId, Long tocUserId);
}

