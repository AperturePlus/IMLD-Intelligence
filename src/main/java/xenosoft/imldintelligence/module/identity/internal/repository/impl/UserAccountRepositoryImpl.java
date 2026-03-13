package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.UserAccountMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;

import java.util.List;
import java.util.Optional;

/**
 * 用户账号仓储实现类，基于 MyBatis Mapper 完成用户账号的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class UserAccountRepositoryImpl implements UserAccountRepository {
    private final UserAccountMapper userAccountMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserAccount> findById(Long tenantId, Long id) {
        return Optional.ofNullable(userAccountMapper.findById(tenantId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserAccount> findByUserNo(Long tenantId, String userNo) {
        return Optional.ofNullable(userAccountMapper.findByUserNo(tenantId, userNo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserAccount> findByUsername(Long tenantId, String username) {
        return Optional.ofNullable(userAccountMapper.findByUsername(tenantId, username));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserAccount> listByTenantId(Long tenantId) {
        return userAccountMapper.listByTenantId(tenantId);
    }

    @Override
    public long countByCondition(Long tenantId, String usernameKeyword, String userType,
                                  String deptName, String status) {
        return userAccountMapper.countByCondition(tenantId, usernameKeyword, userType, deptName, status);
    }

    @Override
    public List<UserAccount> listByCondition(Long tenantId, String usernameKeyword, String userType,
                                              String deptName, String status, long offset, int limit) {
        return userAccountMapper.listByCondition(tenantId, usernameKeyword, userType, deptName, status, offset, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount save(UserAccount userAccount) {
        userAccountMapper.insert(userAccount);
        return userAccount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount update(UserAccount userAccount) {
        userAccountMapper.update(userAccount);
        return userAccount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return userAccountMapper.deleteById(tenantId, id) > 0;
    }
}
