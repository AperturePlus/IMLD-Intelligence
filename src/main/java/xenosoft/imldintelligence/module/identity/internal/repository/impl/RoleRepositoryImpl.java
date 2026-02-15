package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.RoleMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.Role;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {
    private final RoleMapper roleMapper;

    @Override
    public Optional<Role> findById(Long tenantId, Long id) {
        return Optional.ofNullable(roleMapper.findById(tenantId, id));
    }

    @Override
    public Optional<Role> findByRoleCode(Long tenantId, String roleCode) {
        return Optional.ofNullable(roleMapper.findByRoleCode(tenantId, roleCode));
    }

    @Override
    public List<Role> listByTenantId(Long tenantId) {
        return roleMapper.listByTenantId(tenantId);
    }

    @Override
    public Role save(Role role) {
        roleMapper.insert(role);
        return role;
    }

    @Override
    public Role update(Role role) {
        roleMapper.update(role);
        return role;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return roleMapper.deleteById(tenantId, id) > 0;
    }
}
