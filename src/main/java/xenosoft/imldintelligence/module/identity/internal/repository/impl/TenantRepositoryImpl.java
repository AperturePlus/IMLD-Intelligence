package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.TenantMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;

import java.util.List;
import java.util.Optional;

/**
 * 租户仓储实现类，基于 MyBatis Mapper 完成租户的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepository {
    private final TenantMapper tenantMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tenant> findById(Long id) {
        return Optional.ofNullable(tenantMapper.findById(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tenant> findByTenantCode(String tenantCode) {
        return Optional.ofNullable(tenantMapper.findByTenantCode(tenantCode));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tenant> listAll() {
        return tenantMapper.listAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tenant save(Tenant tenant) {
        tenantMapper.insert(tenant);
        return tenant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tenant update(Tenant tenant) {
        tenantMapper.update(tenant);
        return tenant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long id) {
        return tenantMapper.deleteById(id) > 0;
    }
}
