package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.UserRoleRelMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;

import java.util.List;
import java.util.Optional;

/**
 * 用户角色关系仓储实现类，基于 MyBatis Mapper 完成用户角色关系的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRelRepositoryImpl implements UserRoleRelRepository {
    private final UserRoleRelMapper userRoleRelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserRoleRel> findById(Long tenantId, Long id) {
        return Optional.ofNullable(userRoleRelMapper.findById(tenantId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserRoleRel> findByUserIdAndRoleId(Long tenantId, Long userId, Long roleId) {
        return Optional.ofNullable(userRoleRelMapper.findByUserIdAndRoleId(tenantId, userId, roleId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserRoleRel> listByUserId(Long tenantId, Long userId) {
        return userRoleRelMapper.listByUserId(tenantId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserRoleRel> listByRoleId(Long tenantId, Long roleId) {
        return userRoleRelMapper.listByRoleId(tenantId, roleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleRel save(UserRoleRel userRoleRel) {
        userRoleRelMapper.insert(userRoleRel);
        return userRoleRel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleRel update(UserRoleRel userRoleRel) {
        userRoleRelMapper.update(userRoleRel);
        return userRoleRel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return userRoleRelMapper.deleteById(tenantId, id) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteByUserIdAndRoleId(Long tenantId, Long userId, Long roleId) {
        return userRoleRelMapper.deleteByUserIdAndRoleId(tenantId, userId, roleId) > 0;
    }
}
