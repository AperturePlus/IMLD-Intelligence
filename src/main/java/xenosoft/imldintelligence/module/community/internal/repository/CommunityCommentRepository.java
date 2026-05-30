package xenosoft.imldintelligence.module.community.internal.repository;

import xenosoft.imldintelligence.module.community.internal.model.CommunityPostComment;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityCommentPageRow;

import java.util.List;
import java.util.Optional;

public interface CommunityCommentRepository {
    Optional<CommunityPostComment> findById(Long tenantId, Long commentId);

    List<CommunityCommentPageRow> listByPostIdWithTotal(Long tenantId,
                                                        Long postId,
                                                        Long parentCommentId,
                                                        boolean excludeDeleted,
                                                        long offset,
                                                        int limit);

    CommunityPostComment save(CommunityPostComment comment);

    boolean softDelete(Long tenantId, Long commentId);
}
