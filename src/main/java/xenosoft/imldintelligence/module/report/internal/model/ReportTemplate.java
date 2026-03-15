package xenosoft.imldintelligence.module.report.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import xenosoft.imldintelligence.common.mybatis.JsonNodeTypeHandler;

@Data
@TableName(value = "report_template", autoResultMap = true)
public class ReportTemplate {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String templateCode;
    private String templateName;
    private String diseaseCode;
    private String department;
    @TableField(typeHandler = JsonNodeTypeHandler.class)
    private JsonNode templateSchema;
    private String status;
    private Integer versionNo;
    private Long createdBy;
    private java.time.OffsetDateTime createdAt;
}
