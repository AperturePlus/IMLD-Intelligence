package xenosoft.imldintelligence.module.payment.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.payment.internal.model.VipPlan;
import xenosoft.imldintelligence.module.payment.internal.repository.VipPlanRepository;
import xenosoft.imldintelligence.module.payment.internal.repository.mybatis.VipPlanMapper;

import java.util.List;
import java.util.Optional;

/**
 * VIP套餐仓储实现类，基于 MyBatis-Plus 完成VIP套餐的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class VipPlanRepositoryImpl implements VipPlanRepository {
    private final VipPlanMapper vipPlanMapper;

    @Override
    public Optional<VipPlan> findById(Long tenantId, Long id) {
        return Optional.ofNullable(vipPlanMapper.selectOne(new LambdaQueryWrapper<VipPlan>()
                .eq(VipPlan::getTenantId, tenantId)
                .eq(VipPlan::getId, id)));
    }

    @Override
    public Optional<VipPlan> findByPlanCode(Long tenantId, String planCode) {
        return Optional.ofNullable(vipPlanMapper.selectOne(new LambdaQueryWrapper<VipPlan>()
                .eq(VipPlan::getTenantId, tenantId)
                .eq(VipPlan::getPlanCode, planCode)));
    }

    @Override
    public List<VipPlan> listByTenantId(Long tenantId) {
        return vipPlanMapper.selectList(new LambdaQueryWrapper<VipPlan>()
                .eq(VipPlan::getTenantId, tenantId)
                .orderByDesc(VipPlan::getId));
    }

    @Override
    public VipPlan save(VipPlan vipPlan) {
        vipPlanMapper.insert(vipPlan);
        return vipPlan;
    }

    @Override
    public VipPlan update(VipPlan vipPlan) {
        vipPlanMapper.update(vipPlan, new LambdaUpdateWrapper<VipPlan>()
                .eq(VipPlan::getTenantId, vipPlan.getTenantId())
                .eq(VipPlan::getId, vipPlan.getId()));
        return vipPlan;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return vipPlanMapper.update(null, new LambdaUpdateWrapper<VipPlan>()
                .eq(VipPlan::getTenantId, tenantId)
                .eq(VipPlan::getId, id)
                .set(VipPlan::getStatus, "INACTIVE")) > 0;
    }
}
