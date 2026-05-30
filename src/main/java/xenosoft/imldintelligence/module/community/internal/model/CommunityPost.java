package xenosoft.imldintelligence.module.community.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@lombok.Data
@TableName("community_post")
public class CommunityPost {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long boardId;
    private Long authorTocUserId;
    private String authorDisplayName;
    private Boolean anonymousFlag;
    private String title;
    private String content;
    private String contentFormat;
    private String status;
    private Boolean pinnedFlag;
    private Integer likeCount;
    private Integer commentCount;
    private Integer reportCount;
    private java.time.OffsetDateTime lastActivityAt;
    private Long reviewedBy;
    private java.time.OffsetDateTime reviewedAt;
    private String reviewReason;
    private String autoReviewStatus;
    private String autoReviewProvider;
    private String autoReviewReason;
    private java.time.OffsetDateTime autoReviewAt;
    private java.time.OffsetDateTime createdAt;
    private java.time.OffsetDateTime updatedAt;
    private java.time.OffsetDateTime deletedAt;
}
