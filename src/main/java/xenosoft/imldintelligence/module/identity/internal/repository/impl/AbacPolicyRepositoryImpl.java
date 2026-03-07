package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.AbacPolicyRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.AbacPolicyMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;

import java.util.List;
import java.util.Optional;

/**
 * ABAC策略仓储实现类，基于 MyBatis Mapper 完成ABAC策略的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class AbacPolicyRepositoryImpl implements AbacPolicyRepository {
    private final AbacPolicyMapper abacPolicyMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AbacPolicy> findById(Long tenantId, Long id) {
        return Optional.ofNullable(abacPolicyMapper.findById(tenantId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AbacPolicy> findByPolicyCode(Long tenantId, String policyCode) {
        return Optional.ofNullable(abacPolicyMapper.findByPolicyCode(tenantId, policyCode));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbacPolicy> listByTenantId(Long tenantId) {
        return abacPolicyMapper.listByTenantId(tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbacPolicy save(AbacPolicy abacPolicy) {
        abacPolicyMapper.insert(abacPolicy);
        return abacPolicy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbacPolicy update(AbacPolicy abacPolicy) {
        abacPolicyMapper.update(abacPolicy);
        return abacPolicy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return abacPolicyMapper.deleteById(tenantId, id) > 0;
    }
}
