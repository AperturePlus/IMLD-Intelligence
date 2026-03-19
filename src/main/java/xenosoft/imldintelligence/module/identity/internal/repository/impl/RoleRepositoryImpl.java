package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储实现类，基于 MyBatis-Plus 完成角色的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {
    private final RoleMapper roleMapper;

    @Override
    public Optional<Role> findById(Long tenantId, Long id) {
        return Optional.ofNullable(roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getTenantId, tenantId)
                .eq(Role::getId, id)));
    }

    @Override
    public Optional<Role> findByRoleCode(Long tenantId, String roleCode) {
        return Optional.ofNullable(roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getTenantId, tenantId)
                .eq(Role::getRoleCode, roleCode)));
    }

    @Override
    public List<Role> listByTenantId(Long tenantId) {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getTenantId, tenantId)
                .orderByDesc(Role::getId));
    }

    @Override
    public Role save(Role role) {
        roleMapper.insert(role);
        return role;
    }

    @Override
    public Role update(Role role) {
        roleMapper.update(null, new LambdaUpdateWrapper<Role>()
                .eq(Role::getTenantId, role.getTenantId())
                .eq(Role::getId, role.getId())
                .set(Role::getRoleCode, role.getRoleCode())
                .set(Role::getRoleName, role.getRoleName())
                .set(Role::getDescription, role.getDescription())
                .set(Role::getStatus, role.getStatus()));
        return role;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return roleMapper.update(null, new LambdaUpdateWrapper<Role>()
                .eq(Role::getTenantId, tenantId)
                .eq(Role::getId, id)
                .set(Role::getStatus, "INACTIVE")) > 0;
    }
}
