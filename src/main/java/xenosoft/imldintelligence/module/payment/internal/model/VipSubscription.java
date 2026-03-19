package xenosoft.imldintelligence.module.payment.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vip_subscription")
public class VipSubscription {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long tocUserId;
    private Long planId;
    private Long orderId;
    private String subscriptionStatus;
    private java.time.OffsetDateTime startAt;
    private java.time.OffsetDateTime endAt;
    private java.time.OffsetDateTime createdAt;
}
