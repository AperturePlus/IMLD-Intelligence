package xenosoft.imldintelligence.module.community.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@lombok.Data
@TableName("community_post_comment")
public class CommunityPostComment {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long postId;
    private Long parentCommentId;
    private Long authorTocUserId;
    private String authorDisplayName;
    private Boolean anonymousFlag;
    private String content;
    private String status;
    private java.time.OffsetDateTime createdAt;
    private java.time.OffsetDateTime updatedAt;
    private java.time.OffsetDateTime deletedAt;
}

