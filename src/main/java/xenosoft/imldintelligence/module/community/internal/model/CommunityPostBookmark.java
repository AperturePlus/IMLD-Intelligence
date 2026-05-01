package xenosoft.imldintelligence.module.community.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@lombok.Data
@TableName("community_post_bookmark")
public class CommunityPostBookmark {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long postId;
    private Long tocUserId;
    private java.time.OffsetDateTime createdAt;
}

