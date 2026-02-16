package xenosoft.imldintelligence.module.payment.internal.repository;

import xenosoft.imldintelligence.module.payment.internal.model.VipPlan;

import java.util.List;
import java.util.Optional;

public interface VipPlanRepository {
    Optional<VipPlan> findById(Long tenantId, Long id);

    Optional<VipPlan> findByPlanCode(Long tenantId, String planCode);

    List<VipPlan> listByTenantId(Long tenantId);

    VipPlan save(VipPlan vipPlan);

    VipPlan update(VipPlan vipPlan);

    Boolean deleteById(Long tenantId, Long id);
}

