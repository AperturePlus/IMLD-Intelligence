package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.UserRoleRelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户角色关系仓储实现类，基于 MyBatis-Plus 完成用户角色关系的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRelRepositoryImpl implements UserRoleRelRepository {
    private final UserRoleRelMapper userRoleRelMapper;

    @Override
    public Optional<UserRoleRel> findById(Long tenantId, Long id) {
        return Optional.ofNullable(userRoleRelMapper.selectOne(new LambdaQueryWrapper<UserRoleRel>()
                .eq(UserRoleRel::getTenantId, tenantId)
                .eq(UserRoleRel::getId, id)));
    }

    @Override
    public Optional<UserRoleRel> findByUserIdAndRoleId(Long tenantId, Long userId, Long roleId) {
        return Optional.ofNullable(userRoleRelMapper.selectOne(new LambdaQueryWrapper<UserRoleRel>()
                .eq(UserRoleRel::getTenantId, tenantId)
                .eq(UserRoleRel::getUserId, userId)
                .eq(UserRoleRel::getRoleId, roleId)));
    }

    @Override
    public List<UserRoleRel> listByUserId(Long tenantId, Long userId) {
        return userRoleRelMapper.selectList(new LambdaQueryWrapper<UserRoleRel>()
                .eq(UserRoleRel::getTenantId, tenantId)
                .eq(UserRoleRel::getUserId, userId)
                .orderByDesc(UserRoleRel::getId));
    }

    @Override
    public List<UserRoleRel> listByRoleId(Long tenantId, Long roleId) {
        return userRoleRelMapper.selectList(new LambdaQueryWrapper<UserRoleRel>()
                .eq(UserRoleRel::getTenantId, tenantId)
                .eq(UserRoleRel::getRoleId, roleId)
                .orderByDesc(UserRoleRel::getId));
    }

    @Override
    public UserRoleRel save(UserRoleRel userRoleRel) {
        userRoleRelMapper.insert(userRoleRel);
        return userRoleRel;
    }

    @Override
    public UserRoleRel update(UserRoleRel userRoleRel) {
        userRoleRelMapper.update(null, new LambdaUpdateWrapper<UserRoleRel>()
                .eq(UserRoleRel::getTenantId, userRoleRel.getTenantId())
                .eq(UserRoleRel::getId, userRoleRel.getId())
                .set(UserRoleRel::getUserId, userRoleRel.getUserId())
                .set(UserRoleRel::getRoleId, userRoleRel.getRoleId())
                .set(UserRoleRel::getGrantedBy, userRoleRel.getGrantedBy())
                .set(UserRoleRel::getGrantedAt, userRoleRel.getGrantedAt()));
        return userRoleRel;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return userRoleRelMapper.delete(new LambdaQueryWrapper<UserRoleRel>()
                .eq(UserRoleRel::getTenantId, tenantId)
                .eq(UserRoleRel::getId, id)) > 0;
    }

    @Override
    public Boolean deleteByUserIdAndRoleId(Long tenantId, Long userId, Long roleId) {
        return userRoleRelMapper.delete(new LambdaQueryWrapper<UserRoleRel>()
                .eq(UserRoleRel::getTenantId, tenantId)
                .eq(UserRoleRel::getUserId, userId)
                .eq(UserRoleRel::getRoleId, roleId)) > 0;
    }
}
