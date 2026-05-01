package xenosoft.imldintelligence.module.community.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@lombok.Data
@TableName("community_content_report")
public class CommunityContentReport {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long reporterTocUserId;
    private Long postId;
    private Long commentId;
    private String reasonCode;
    private String reasonText;
    private String status;
    private Long handledBy;
    private java.time.OffsetDateTime handledAt;
    private String resultAction;
    private String resultNote;
    private java.time.OffsetDateTime createdAt;
}

