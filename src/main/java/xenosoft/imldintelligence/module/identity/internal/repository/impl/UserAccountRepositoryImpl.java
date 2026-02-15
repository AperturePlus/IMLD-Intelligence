package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.UserAccountMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAccountRepositoryImpl implements UserAccountRepository {
    private final UserAccountMapper userAccountMapper;

    @Override
    public Optional<UserAccount> findById(Long tenantId, Long id) {
        return Optional.ofNullable(userAccountMapper.findById(tenantId, id));
    }

    @Override
    public Optional<UserAccount> findByUserNo(Long tenantId, String userNo) {
        return Optional.ofNullable(userAccountMapper.findByUserNo(tenantId, userNo));
    }

    @Override
    public Optional<UserAccount> findByUsername(Long tenantId, String username) {
        return Optional.ofNullable(userAccountMapper.findByUsername(tenantId, username));
    }

    @Override
    public List<UserAccount> listByTenantId(Long tenantId) {
        return userAccountMapper.listByTenantId(tenantId);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        userAccountMapper.insert(userAccount);
        return userAccount;
    }

    @Override
    public UserAccount update(UserAccount userAccount) {
        userAccountMapper.update(userAccount);
        return userAccount;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return userAccountMapper.deleteById(tenantId, id) > 0;
    }
}
