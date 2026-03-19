package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;
import xenosoft.imldintelligence.module.identity.internal.repository.AbacPolicyRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.AbacPolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ABAC策略仓储实现类，基于 MyBatis-Plus 完成ABAC策略的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class AbacPolicyRepositoryImpl implements AbacPolicyRepository {
    private final AbacPolicyMapper abacPolicyMapper;

    @Override
    public Optional<AbacPolicy> findById(Long tenantId, Long id) {
        return Optional.ofNullable(abacPolicyMapper.selectOne(new LambdaQueryWrapper<AbacPolicy>()
                .eq(AbacPolicy::getTenantId, tenantId)
                .eq(AbacPolicy::getId, id)));
    }

    @Override
    public Optional<AbacPolicy> findByPolicyCode(Long tenantId, String policyCode) {
        return Optional.ofNullable(abacPolicyMapper.selectOne(new LambdaQueryWrapper<AbacPolicy>()
                .eq(AbacPolicy::getTenantId, tenantId)
                .eq(AbacPolicy::getPolicyCode, policyCode)));
    }

    @Override
    public List<AbacPolicy> listByTenantId(Long tenantId) {
        return abacPolicyMapper.selectList(new LambdaQueryWrapper<AbacPolicy>()
                .eq(AbacPolicy::getTenantId, tenantId)
                .orderByDesc(AbacPolicy::getId));
    }

    @Override
    public AbacPolicy save(AbacPolicy abacPolicy) {
        abacPolicyMapper.insert(abacPolicy);
        return abacPolicy;
    }

    @Override
    public AbacPolicy update(AbacPolicy abacPolicy) {
        abacPolicyMapper.update(null, new LambdaUpdateWrapper<AbacPolicy>()
                .eq(AbacPolicy::getTenantId, abacPolicy.getTenantId())
                .eq(AbacPolicy::getId, abacPolicy.getId())
                .set(AbacPolicy::getPolicyCode, abacPolicy.getPolicyCode())
                .set(AbacPolicy::getPolicyName, abacPolicy.getPolicyName())
                .set(AbacPolicy::getSubjectExpr, abacPolicy.getSubjectExpr())
                .set(AbacPolicy::getResourceExpr, abacPolicy.getResourceExpr())
                .set(AbacPolicy::getActionExpr, abacPolicy.getActionExpr())
                .set(AbacPolicy::getEffect, abacPolicy.getEffect())
                .set(AbacPolicy::getPriority, abacPolicy.getPriority())
                .set(AbacPolicy::getStatus, abacPolicy.getStatus()));
        return abacPolicy;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return abacPolicyMapper.update(null, new LambdaUpdateWrapper<AbacPolicy>()
                .eq(AbacPolicy::getTenantId, tenantId)
                .eq(AbacPolicy::getId, id)
                .set(AbacPolicy::getStatus, "INACTIVE")) > 0;
    }
}
