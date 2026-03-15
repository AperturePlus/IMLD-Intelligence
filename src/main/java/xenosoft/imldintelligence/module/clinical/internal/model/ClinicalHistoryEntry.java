package xenosoft.imldintelligence.module.clinical.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import xenosoft.imldintelligence.common.mybatis.JsonNodeTypeHandler;

@Data
@TableName(value = "clinical_history_entry", autoResultMap = true)
public class ClinicalHistoryEntry {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long patientId;
    private Long encounterId;
    private String historyType;
    private String templateCode;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode contentJson;
    private String sourceType;
    private Long recordedBy;
    private java.time.OffsetDateTime recordedAt;
    private java.time.OffsetDateTime createdAt;
}
