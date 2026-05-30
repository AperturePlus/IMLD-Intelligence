package xenosoft.imldintelligence.module.community.internal.repository.mybatis;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 帖子分页查询行，承载窗口函数总数列，避免重复 count SQL。
 */
@Data
public class CommunityPostSummaryRow {
    private Long id;
    private Long tenantId;
    private Long boardId;
    private Long authorTocUserId;
    private String authorDisplayName;
    private Boolean anonymousFlag;
    private String title;
    private String contentExcerpt;
    private String status;
    private Boolean pinnedFlag;
    private Integer likeCount;
    private Integer commentCount;
    private Integer reportCount;
    private OffsetDateTime lastActivityAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    private Long totalCount;
}

