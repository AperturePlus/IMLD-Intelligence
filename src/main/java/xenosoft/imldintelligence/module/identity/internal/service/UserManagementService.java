package xenosoft.imldintelligence.module.identity.internal.service;

import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;

import java.util.List;

public interface UserManagementService {

    long countUsers(Long tenantId, IdentityApiDtos.Query.UserAccountPageQuery query);

    List<UserAccount> listUsers(Long tenantId, IdentityApiDtos.Query.UserAccountPageQuery query,
                                long offset, int limit);

    UserAccount grantRole(Long tenantId, Long userId, IdentityApiDtos.Request.GrantRoleRequest request);
}
