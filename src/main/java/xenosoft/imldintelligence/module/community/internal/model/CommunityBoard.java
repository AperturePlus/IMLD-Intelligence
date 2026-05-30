package xenosoft.imldintelligence.module.community.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@lombok.Data
@TableName("community_board")
public class CommunityBoard {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String boardCode;
    private String boardName;
    private String description;
    private String diseaseScope;
    private String status;
    private Integer sortOrder;
    private java.time.OffsetDateTime createdAt;
    private java.time.OffsetDateTime updatedAt;
}

