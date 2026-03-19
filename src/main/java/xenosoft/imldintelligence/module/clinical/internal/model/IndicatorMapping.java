package xenosoft.imldintelligence.module.clinical.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import xenosoft.imldintelligence.common.mybatis.JsonNodeTypeHandler;

@Data
@TableName(value = "indicator_mapping", autoResultMap = true)
public class IndicatorMapping {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String sourceSystem;
    private String sourceCode;
    private String sourceName;
    private String targetIndicatorCode;
    private String unitConversionExpr;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode qualityRule;
    private String status;
    private java.time.OffsetDateTime createdAt;
}
