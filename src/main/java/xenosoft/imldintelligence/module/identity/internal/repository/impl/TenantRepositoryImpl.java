package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.TenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 租户仓储实现类，基于 MyBatis-Plus 完成租户的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepository {
    private final TenantMapper tenantMapper;

    @Override
    public Optional<Tenant> findById(Long id) {
        return Optional.ofNullable(tenantMapper.selectById(id));
    }

    @Override
    public Optional<Tenant> findByTenantCode(String tenantCode) {
        return Optional.ofNullable(tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getTenantCode, tenantCode)));
    }

    @Override
    public List<Tenant> listAll() {
        return tenantMapper.selectList(new LambdaQueryWrapper<Tenant>().orderByDesc(Tenant::getId));
    }

    @Override
    public Tenant save(Tenant tenant) {
        tenantMapper.insert(tenant);
        return tenant;
    }

    @Override
    public Tenant update(Tenant tenant) {
        tenantMapper.update(null, new LambdaUpdateWrapper<Tenant>()
                .eq(Tenant::getId, tenant.getId())
                .set(Tenant::getTenantCode, tenant.getTenantCode())
                .set(Tenant::getTenantName, tenant.getTenantName())
                .set(Tenant::getDeployMode, tenant.getDeployMode())
                .set(Tenant::getStatus, tenant.getStatus()));
        return tenant;
    }

    @Override
    public Boolean deleteById(Long id) {
        return tenantMapper.update(null, new LambdaUpdateWrapper<Tenant>()
                .eq(Tenant::getId, id)
                .set(Tenant::getStatus, "INACTIVE")) > 0;
    }
}
