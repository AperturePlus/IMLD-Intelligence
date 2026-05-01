package xenosoft.imldintelligence.module.community.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.community.internal.model.CommunityPost;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityPostRepository;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityPostMapper;
import xenosoft.imldintelligence.module.community.internal.repository.mybatis.CommunityPostSummaryRow;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommunityPostRepositoryImpl implements CommunityPostRepository {
    private final CommunityPostMapper communityPostMapper;

    @Override
    public Optional<CommunityPost> findById(Long tenantId, Long postId) {
        return Optional.ofNullable(communityPostMapper.selectOne(new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getTenantId, tenantId)
                .eq(CommunityPost::getId, postId)));
    }

    @Override
    public List<CommunityPostSummaryRow> listSummariesByConditionWithTotal(Long tenantId,
                                                                          Long boardId,
                                                                          Long authorTocUserId,
                                                                          String status,
                                                                          String keyword,
                                                                          boolean excludeDeleted,
                                                                          long offset,
                                                                          int limit) {
        return communityPostMapper.listSummariesByConditionWithTotal(
                tenantId,
                boardId,
                authorTocUserId,
                status,
                keyword,
                excludeDeleted,
                offset,
                limit
        );
    }

    @Override
    public CommunityPost save(CommunityPost post) {
        communityPostMapper.insert(post);
        return post;
    }

    @Override
    public CommunityPost update(CommunityPost post) {
        OffsetDateTime now = OffsetDateTime.now();
        communityPostMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getTenantId, post.getTenantId())
                .eq(CommunityPost::getId, post.getId())
                .set(CommunityPost::getBoardId, post.getBoardId())
                .set(CommunityPost::getAuthorTocUserId, post.getAuthorTocUserId())
                .set(CommunityPost::getAuthorDisplayName, post.getAuthorDisplayName())
                .set(CommunityPost::getAnonymousFlag, post.getAnonymousFlag())
                .set(CommunityPost::getTitle, post.getTitle())
                .set(CommunityPost::getContent, post.getContent())
                .set(CommunityPost::getContentFormat, post.getContentFormat())
                .set(CommunityPost::getStatus, post.getStatus())
                .set(CommunityPost::getPinnedFlag, post.getPinnedFlag())
                .set(CommunityPost::getLikeCount, post.getLikeCount())
                .set(CommunityPost::getCommentCount, post.getCommentCount())
                .set(CommunityPost::getReportCount, post.getReportCount())
                .set(CommunityPost::getLastActivityAt, post.getLastActivityAt())
                .set(CommunityPost::getReviewedBy, post.getReviewedBy())
                .set(CommunityPost::getReviewedAt, post.getReviewedAt())
                .set(CommunityPost::getReviewReason, post.getReviewReason())
                .set(CommunityPost::getAutoReviewStatus, post.getAutoReviewStatus())
                .set(CommunityPost::getAutoReviewProvider, post.getAutoReviewProvider())
                .set(CommunityPost::getAutoReviewReason, post.getAutoReviewReason())
                .set(CommunityPost::getAutoReviewAt, post.getAutoReviewAt())
                .set(CommunityPost::getDeletedAt, post.getDeletedAt())
                .set(CommunityPost::getUpdatedAt, now));
        post.setUpdatedAt(now);
        return post;
    }

    @Override
    public boolean incrementLikeCount(Long tenantId, Long postId) {
        return communityPostMapper.incrementLikeCount(tenantId, postId) > 0;
    }

    @Override
    public boolean decrementLikeCount(Long tenantId, Long postId) {
        return communityPostMapper.decrementLikeCount(tenantId, postId) > 0;
    }

    @Override
    public boolean incrementCommentCount(Long tenantId, Long postId) {
        return communityPostMapper.incrementCommentCount(tenantId, postId) > 0;
    }

    @Override
    public boolean incrementReportCount(Long tenantId, Long postId) {
        return communityPostMapper.incrementReportCount(tenantId, postId) > 0;
    }

    @Override
    public boolean updateModeration(Long tenantId, Long postId, String status, String reviewReason, Long reviewedBy, Boolean pinnedFlag) {
        return communityPostMapper.updateModeration(tenantId, postId, status, reviewReason, reviewedBy, pinnedFlag) > 0;
    }
}
