package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.UserRoleRelMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRoleRelRepositoryImpl implements UserRoleRelRepository {
    private final UserRoleRelMapper userRoleRelMapper;

    @Override
    public Optional<UserRoleRel> findById(Long tenantId, Long id) {
        return Optional.ofNullable(userRoleRelMapper.findById(tenantId, id));
    }

    @Override
    public Optional<UserRoleRel> findByUserIdAndRoleId(Long tenantId, Long userId, Long roleId) {
        return Optional.ofNullable(userRoleRelMapper.findByUserIdAndRoleId(tenantId, userId, roleId));
    }

    @Override
    public List<UserRoleRel> listByUserId(Long tenantId, Long userId) {
        return userRoleRelMapper.listByUserId(tenantId, userId);
    }

    @Override
    public List<UserRoleRel> listByRoleId(Long tenantId, Long roleId) {
        return userRoleRelMapper.listByRoleId(tenantId, roleId);
    }

    @Override
    public UserRoleRel save(UserRoleRel userRoleRel) {
        userRoleRelMapper.insert(userRoleRel);
        return userRoleRel;
    }

    @Override
    public UserRoleRel update(UserRoleRel userRoleRel) {
        userRoleRelMapper.update(userRoleRel);
        return userRoleRel;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return userRoleRelMapper.deleteById(tenantId, id) > 0;
    }

    @Override
    public Boolean deleteByUserIdAndRoleId(Long tenantId, Long userId, Long roleId) {
        return userRoleRelMapper.deleteByUserIdAndRoleId(tenantId, userId, roleId) > 0;
    }
}
