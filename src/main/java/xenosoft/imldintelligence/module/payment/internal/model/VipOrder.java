package xenosoft.imldintelligence.module.payment.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vip_order")
public class VipOrder {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String orderNo;
    private Long tocUserId;
    private Long planId;
    private String orderStatus;
    private java.math.BigDecimal amount;
    private java.time.OffsetDateTime paidAt;
    private String channel;
    private java.time.OffsetDateTime createdAt;
}
