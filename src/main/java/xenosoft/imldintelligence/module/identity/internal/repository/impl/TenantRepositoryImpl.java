package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.TenantMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepository {
    private final TenantMapper tenantMapper;

    @Override
    public Optional<Tenant> findById(Long id) {
        return Optional.ofNullable(tenantMapper.findById(id));
    }

    @Override
    public Optional<Tenant> findByTenantCode(String tenantCode) {
        return Optional.ofNullable(tenantMapper.findByTenantCode(tenantCode));
    }

    @Override
    public List<Tenant> listAll() {
        return tenantMapper.listAll();
    }

    @Override
    public Tenant save(Tenant tenant) {
        tenantMapper.insert(tenant);
        return tenant;
    }

    @Override
    public Tenant update(Tenant tenant) {
        tenantMapper.update(tenant);
        return tenant;
    }

    @Override
    public Boolean deleteById(Long id) {
        return tenantMapper.deleteById(id) > 0;
    }
}
