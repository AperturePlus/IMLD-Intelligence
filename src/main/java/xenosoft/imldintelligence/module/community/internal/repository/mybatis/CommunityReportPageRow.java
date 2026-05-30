package xenosoft.imldintelligence.module.community.internal.repository.mybatis;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 举报分页查询行，承载窗口函数总数列，避免重复 count SQL。
 */
@Data
public class CommunityReportPageRow {
    private Long id;
    private Long tenantId;
    private Long reporterTocUserId;
    private Long postId;
    private Long commentId;
    private String reasonCode;
    private String reasonText;
    private String status;
    private Long handledBy;
    private OffsetDateTime handledAt;
    private String resultAction;
    private String resultNote;
    private OffsetDateTime createdAt;
    private Long totalCount;
}

