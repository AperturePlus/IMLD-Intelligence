package xenosoft.imldintelligence.module.community.internal.repository;

import xenosoft.imldintelligence.module.community.internal.model.CommunityPost;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityPostSummaryRow;

import java.util.List;
import java.util.Optional;

public interface CommunityPostRepository {
    Optional<CommunityPost> findById(Long tenantId, Long postId);

    List<CommunityPostSummaryRow> listSummariesByConditionWithTotal(Long tenantId,
                                                                    Long boardId,
                                                                    Long authorTocUserId,
                                                                    String status,
                                                                    String keyword,
                                                                    boolean excludeDeleted,
                                                                    long offset,
                                                                    int limit);

    CommunityPost save(CommunityPost post);

    CommunityPost update(CommunityPost post);

    boolean incrementLikeCount(Long tenantId, Long postId);

    boolean decrementLikeCount(Long tenantId, Long postId);

    boolean incrementCommentCount(Long tenantId, Long postId);

    boolean incrementReportCount(Long tenantId, Long postId);

    boolean updateModeration(Long tenantId, Long postId, String status, String reviewReason, Long reviewedBy, Boolean pinnedFlag);
}

