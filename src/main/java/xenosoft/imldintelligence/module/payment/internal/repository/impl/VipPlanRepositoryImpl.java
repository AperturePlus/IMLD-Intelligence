package xenosoft.imldintelligence.module.payment.internal.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.payment.internal.model.VipPlan;
import xenosoft.imldintelligence.module.payment.internal.repository.VipPlanRepository;
import xenosoft.imldintelligence.module.payment.internal.repository.mybatis.VipPlanMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VipPlanRepositoryImpl implements VipPlanRepository {
    private final VipPlanMapper vipPlanMapper;

    @Override
    public Optional<VipPlan> findById(Long tenantId, Long id) {
        return Optional.ofNullable(vipPlanMapper.findById(tenantId, id));
    }

    @Override
    public Optional<VipPlan> findByPlanCode(Long tenantId, String planCode) {
        return Optional.ofNullable(vipPlanMapper.findByPlanCode(tenantId, planCode));
    }

    @Override
    public List<VipPlan> listByTenantId(Long tenantId) {
        return vipPlanMapper.listByTenantId(tenantId);
    }

    @Override
    public VipPlan save(VipPlan vipPlan) {
        vipPlanMapper.insert(vipPlan);
        return vipPlan;
    }

    @Override
    public VipPlan update(VipPlan vipPlan) {
        vipPlanMapper.update(vipPlan);
        return vipPlan;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return vipPlanMapper.deleteById(tenantId, id) > 0;
    }
}

