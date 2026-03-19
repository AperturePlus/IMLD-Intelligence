package xenosoft.imldintelligence.module.report.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import xenosoft.imldintelligence.common.mybatis.JsonNodeTypeHandler;

@Data
@TableName(value = "report_version", autoResultMap = true)
public class ReportVersion {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long reportId;
    private Integer versionNum;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode contentSnapshot;
    private String changeSummary;
    private Long changedBy;
    private java.time.OffsetDateTime createdAt;
}
