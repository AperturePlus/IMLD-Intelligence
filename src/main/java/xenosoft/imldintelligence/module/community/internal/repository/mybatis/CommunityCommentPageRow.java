package xenosoft.imldintelligence.module.community.internal.repository.mybatis;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 评论分页查询行，承载窗口函数总数列，避免重复 count SQL。
 */
@Data
public class CommunityCommentPageRow {
    private Long id;
    private Long tenantId;
    private Long postId;
    private Long parentCommentId;
    private Long authorTocUserId;
    private String authorDisplayName;
    private Boolean anonymousFlag;
    private String content;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    private Long totalCount;
}

