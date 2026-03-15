package xenosoft.imldintelligence.module.clinical.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("genetic_report")
public class GeneticReport {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long patientId;
    private Long encounterId;
    private String reportSource;
    private java.time.LocalDate reportDate;
    private Long fileId;
    private String parseStatus;
    private String summary;
    private String conclusion;
    private java.time.OffsetDateTime createdAt;
}
