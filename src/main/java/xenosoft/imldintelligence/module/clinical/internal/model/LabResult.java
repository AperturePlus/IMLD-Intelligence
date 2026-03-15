package xenosoft.imldintelligence.module.clinical.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import xenosoft.imldintelligence.common.mybatis.JsonNodeTypeHandler;

@Data
@TableName(value = "lab_result", autoResultMap = true)
public class LabResult {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long patientId;
    private Long encounterId;
    private String indicatorCode;
    private Double valueNumeric;
    private String valueText;
    private String unit;
    private Double referenceLow;
    private Double referenceHigh;
    private String abnormalFlag;
    private String sourceType;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode rawData;
    private java.time.OffsetDateTime collectedAt;
    private java.time.OffsetDateTime createdAt;
}
