package xenosoft.imldintelligence.module.identity.internal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.mybatis.UserAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户账号仓储实现类，基于 MyBatis-Plus 完成用户账号的数据持久化。
 */
@Repository
@RequiredArgsConstructor
public class UserAccountRepositoryImpl implements UserAccountRepository {
    private final UserAccountMapper userAccountMapper;

    @Override
    public Optional<UserAccount> findById(Long tenantId, Long id) {
        return Optional.ofNullable(userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getTenantId, tenantId)
                .eq(UserAccount::getId, id)));
    }

    @Override
    public Optional<UserAccount> findByUserNo(Long tenantId, String userNo) {
        return Optional.ofNullable(userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getTenantId, tenantId)
                .eq(UserAccount::getUserNo, userNo)));
    }

    @Override
    public Optional<UserAccount> findByUsername(Long tenantId, String username) {
        return Optional.ofNullable(userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getTenantId, tenantId)
                .eq(UserAccount::getUsername, username)));
    }

    @Override
    public List<UserAccount> listByTenantId(Long tenantId) {
        return userAccountMapper.selectList(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getTenantId, tenantId)
                .orderByDesc(UserAccount::getId));
    }

    @Override
    public long countByCondition(Long tenantId, String usernameKeyword, String userType,
                                 String deptName, String status) {
        return userAccountMapper.selectCount(buildConditionWrapper(tenantId, usernameKeyword, userType, deptName, status));
    }

    @Override
    public List<UserAccount> listByCondition(Long tenantId, String usernameKeyword, String userType,
                                             String deptName, String status, long offset, int limit) {
        LambdaQueryWrapper<UserAccount> wrapper = buildConditionWrapper(tenantId, usernameKeyword, userType, deptName, status)
                .orderByDesc(UserAccount::getId)
                .last("LIMIT " + limit + " OFFSET " + offset);
        return userAccountMapper.selectList(wrapper);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        userAccountMapper.insert(userAccount);
        return userAccount;
    }

    @Override
    public UserAccount update(UserAccount userAccount) {
        userAccountMapper.update(null, new LambdaUpdateWrapper<UserAccount>()
                .eq(UserAccount::getTenantId, userAccount.getTenantId())
                .eq(UserAccount::getId, userAccount.getId())
                .set(UserAccount::getUserNo, userAccount.getUserNo())
                .set(UserAccount::getUsername, userAccount.getUsername())
                .set(UserAccount::getPasswordHash, userAccount.getPasswordHash())
                .set(UserAccount::getDisplayName, userAccount.getDisplayName())
                .set(UserAccount::getUserType, userAccount.getUserType())
                .set(UserAccount::getDeptName, userAccount.getDeptName())
                .set(UserAccount::getMobileEncrypted, userAccount.getMobileEncrypted())
                .set(UserAccount::getEmail, userAccount.getEmail())
                .set(UserAccount::getStatus, userAccount.getStatus())
                .set(UserAccount::getLastLoginAt, userAccount.getLastLoginAt())
                .set(UserAccount::getUpdatedAt, OffsetDateTime.now()));
        return userAccount;
    }

    @Override
    public Boolean deleteById(Long tenantId, Long id) {
        return userAccountMapper.update(null, new LambdaUpdateWrapper<UserAccount>()
                .eq(UserAccount::getTenantId, tenantId)
                .eq(UserAccount::getId, id)
                .set(UserAccount::getStatus, "INACTIVE")
                .set(UserAccount::getUpdatedAt, OffsetDateTime.now())) > 0;
    }

    private LambdaQueryWrapper<UserAccount> buildConditionWrapper(Long tenantId, String usernameKeyword,
                                                                  String userType, String deptName,
                                                                  String status) {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getTenantId, tenantId)
                .eq(userType != null && !userType.isBlank(), UserAccount::getUserType, userType)
                .eq(deptName != null && !deptName.isBlank(), UserAccount::getDeptName, deptName)
                .eq(status != null && !status.isBlank(), UserAccount::getStatus, status);

        if (usernameKeyword != null && !usernameKeyword.isBlank()) {
            wrapper.and(w -> w.like(UserAccount::getUsername, usernameKeyword)
                    .or()
                    .like(UserAccount::getDisplayName, usernameKeyword));
        }
        return wrapper;
    }
}
