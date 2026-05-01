package xenosoft.imldintelligence.module.community.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.community.internal.model.CommunityPostComment;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityCommentRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityCommentPageRow;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityPostCommentMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommunityCommentRepositoryImpl implements CommunityCommentRepository {
    private final CommunityPostCommentMapper communityPostCommentMapper;

    @Override
    public Optional<CommunityPostComment> findById(Long tenantId, Long commentId) {
        return Optional.ofNullable(communityPostCommentMapper.selectOne(new LambdaQueryWrapper<CommunityPostComment>()
                .eq(CommunityPostComment::getTenantId, tenantId)
                .eq(CommunityPostComment::getId, commentId)));
    }

    @Override
    public List<CommunityCommentPageRow> listByPostIdWithTotal(Long tenantId,
                                                              Long postId,
                                                              Long parentCommentId,
                                                              boolean excludeDeleted,
                                                              long offset,
                                                              int limit) {
        return communityPostCommentMapper.listByPostIdWithTotal(tenantId, postId, parentCommentId, excludeDeleted, offset, limit);
    }

    @Override
    public CommunityPostComment save(CommunityPostComment comment) {
        communityPostCommentMapper.insert(comment);
        return comment;
    }

    @Override
    public boolean softDelete(Long tenantId, Long commentId) {
        OffsetDateTime now = OffsetDateTime.now();
        return communityPostCommentMapper.update(null, new LambdaUpdateWrapper<CommunityPostComment>()
                .eq(CommunityPostComment::getTenantId, tenantId)
                .eq(CommunityPostComment::getId, commentId)
                .set(CommunityPostComment::getStatus, "DELETED")
                .set(CommunityPostComment::getDeletedAt, now)
                .set(CommunityPostComment::getUpdatedAt, now)) > 0;
    }
}
