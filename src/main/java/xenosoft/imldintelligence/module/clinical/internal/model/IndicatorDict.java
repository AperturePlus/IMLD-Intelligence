package xenosoft.imldintelligence.module.clinical.internal.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("indicator_dict")
public class IndicatorDict {
    @TableId("code")
    private String code;
    private String indicatorName;
    private String category;
    private String dataType;
    private String defaultUnit;
    private String loincCode;
    private java.math.BigDecimal normalLow;
    private java.math.BigDecimal normalHigh;
    private String status;
    private java.time.OffsetDateTime createdAt;
}
