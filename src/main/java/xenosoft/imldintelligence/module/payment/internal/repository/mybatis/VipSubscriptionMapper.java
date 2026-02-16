package xenosoft.imldintelligence.module.payment.internal.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xenosoft.imldintelligence.module.payment.internal.model.VipSubscription;

import java.util.List;

@Mapper
public interface VipSubscriptionMapper {
    VipSubscription findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    List<VipSubscription> listByTenantId(@Param("tenantId") Long tenantId);

    List<VipSubscription> listByTocUserId(@Param("tenantId") Long tenantId, @Param("tocUserId") Long tocUserId);

    List<VipSubscription> listByOrderId(@Param("tenantId") Long tenantId, @Param("orderId") Long orderId);

    int insert(VipSubscription vipSubscription);

    int update(VipSubscription vipSubscription);

    int deleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

