package xenosoft.imldintelligence.module.clinical.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("imaging_report")
public class ImagingReport {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long patientId;
    private Long encounterId;
    private String modality;
    private String reportText;
    private Long fileId;
    private String sourceSystem;
    private java.time.OffsetDateTime examinedAt;
    private java.time.OffsetDateTime createdAt;
}
