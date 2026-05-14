package xenosoft.imldintelligence.module.identity.internal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;
import xenosoft.imldintelligence.module.identity.internal.service.UserManagementService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public class UserManagementServiceImpl implements UserManagementService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRelRepository userRoleRelRepository;
    private final RoleRepository roleRepository;

    @Override
    public long countUsers(Long tenantId, IdentityApiDtos.Query.UserAccountPageQuery query) {
        return userAccountRepository.countByCondition(tenantId,
                query.usernameKeyword(), query.userType(),
                query.deptName(), query.status());
    }

    @Override
    public List<UserAccount> listUsers(Long tenantId, IdentityApiDtos.Query.UserAccountPageQuery query,
                                        long offset, int limit) {
        return userAccountRepository.listByCondition(tenantId,
                query.usernameKeyword(), query.userType(),
                query.deptName(), query.status(),
                offset, limit);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserAccount grantRole(Long tenantId, Long userId,
                                  IdentityApiDtos.Request.GrantRoleRequest request) {
        UserAccount user = userAccountRepository.findById(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        roleRepository.findById(tenantId, request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + request.roleId()));

        // Idempotent: check if already granted
        if (userRoleRelRepository.findByUserIdAndRoleId(tenantId, userId, request.roleId()).isEmpty()) {
            UserRoleRel rel = new UserRoleRel();
            rel.setTenantId(tenantId);
            rel.setUserId(userId);
            rel.setRoleId(request.roleId());
            rel.setGrantedBy(request.grantedBy());
            try {
                userRoleRelRepository.save(rel);
            } catch (DataIntegrityViolationException ignored) {
                // Idempotent under concurrent role grants.
            }
        }

        return user;
    }
}
