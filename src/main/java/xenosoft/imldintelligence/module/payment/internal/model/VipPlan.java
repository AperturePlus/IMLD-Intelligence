package xenosoft.imldintelligence.module.payment.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vip_plan")
public class VipPlan {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String planCode;
    private String planName;
    private Integer durationDays;
    private java.math.BigDecimal priceAmount;
    private String currencyCode;
    private String status;
    private java.time.OffsetDateTime createdAt;
}
