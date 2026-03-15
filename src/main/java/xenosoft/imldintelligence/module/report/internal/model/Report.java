package xenosoft.imldintelligence.module.report.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import xenosoft.imldintelligence.common.mybatis.JsonNodeTypeHandler;

@Data
@TableName(value = "report", autoResultMap = true)
public class Report {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long patientId;
    private Long encounterId;
    private Long sessionId;
    private Long templateId;
    private String reportNo;
    private String status;
    private Integer currentVersion;
    private Long signedBy;
    private java.time.OffsetDateTime signedAt;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode signatureData;
    private Long createdBy;
    private java.time.OffsetDateTime createdAt;
    private java.time.OffsetDateTime updatedAt;
}
