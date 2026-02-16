package xenosoft.imldintelligence.module.payment.internal.repository;

import xenosoft.imldintelligence.module.payment.internal.model.VipOrder;

import java.util.List;
import java.util.Optional;

public interface VipOrderRepository {
    Optional<VipOrder> findById(Long tenantId, Long id);

    Optional<VipOrder> findByOrderNo(Long tenantId, String orderNo);

    List<VipOrder> listByTenantId(Long tenantId);

    List<VipOrder> listByTocUserId(Long tenantId, Long tocUserId);

    VipOrder save(VipOrder vipOrder);

    VipOrder update(VipOrder vipOrder);

    Boolean deleteById(Long tenantId, Long id);
}

