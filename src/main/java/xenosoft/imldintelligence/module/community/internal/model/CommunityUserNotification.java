package xenosoft.imldintelligence.module.community.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@lombok.Data
@TableName("community_user_notification")
public class CommunityUserNotification {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long tocUserId;
    private String type;
    private String title;
    private String content;
    private Long relatedPostId;
    private Long relatedCommentId;
    private Boolean isRead;
    private java.time.OffsetDateTime createdAt;
}
