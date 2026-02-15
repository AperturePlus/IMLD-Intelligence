package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.AbacPolicyRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.AbacPolicyMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AbacPolicyRepositoryImpl implements AbacPolicyRepository {
    private final AbacPolicyMapper abacPolicyMapper;

    @Override
    public Optional<AbacPolicy> findById(Long tenantId, Long id) {
        return Optional.ofNullable(abacPolicyMapper.findById(tenantId, id));
    }

    @Override
    public Optional<AbacPolicy> findByPolicyCode(Long tenantId, String policyCode) {
        return Optional.ofNullable(abacPolicyMapper.findByPolicyCode(tenantId, policyCode));
    }

    @Override
    public List<AbacPolicy> listByTenantId(Long tenantId) {
        return abacPolicyMapper.listByTenantId(tenantId);
    }

    @Override
    public AbacPolicy save(AbacPolicy abacPolicy) {
        abacPolicyMapper.insert(abacPolicy);
        return abacPolicy;
    }

    @Override
    public AbacPolicy update(AbacPolicy abacPolicy) {
        abacPolicyMapper.update(abacPolicy);
        return abacPolicy;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return abacPolicyMapper.deleteById(tenantId, id) > 0;
    }
}
