package xenosoft.imldintelligence.module.clinical.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("genetic_variant")
public class GeneticVariant {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long reportId;
    private String gene;
    private String chromosome;
    private Long position;
    private String refAllele;
    private String altAllele;
    private String variantType;
    private String zygosity;
    private String classification;
    private String hgvsC;
    private String hgvsP;
    private String evidence;
    private String sourceType;
    private java.time.OffsetDateTime createdAt;
}
