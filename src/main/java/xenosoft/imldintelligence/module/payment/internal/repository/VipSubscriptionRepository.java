package xenosoft.imldintelligence.module.payment.internal.repository;

import xenosoft.imldintelligence.module.payment.internal.model.VipSubscription;

import java.util.List;
import java.util.Optional;

public interface VipSubscriptionRepository {
    Optional<VipSubscription> findById(Long tenantId, Long id);

    List<VipSubscription> listByTenantId(Long tenantId);

    List<VipSubscription> listByTocUserId(Long tenantId, Long tocUserId);

    List<VipSubscription> listByOrderId(Long tenantId, Long orderId);

    VipSubscription save(VipSubscription vipSubscription);

    VipSubscription update(VipSubscription vipSubscription);

    Boolean deleteById(Long tenantId, Long id);
}

