package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private UserRoleRelRepository userRoleRelRepository;
    @Mock private RoleRepository roleRepository;

    private UserManagementServiceImpl userManagementService;

    @BeforeEach
    void setUp() {
        userManagementService = new UserManagementServiceImpl(
                userAccountRepository, userRoleRelRepository, roleRepository);
    }

    @Test
    void countUsersForwardsToRepository() {
        var query = new IdentityApiDtos.Query.UserAccountPageQuery("doc", "DOCTOR", null, "ACTIVE");
        when(userAccountRepository.countByCondition(1L, "doc", "DOCTOR", null, "ACTIVE"))
                .thenReturn(5L);

        assertThat(userManagementService.countUsers(1L, query)).isEqualTo(5L);
    }

    @Test
    void listUsersForwardsToRepository() {
        var query = new IdentityApiDtos.Query.UserAccountPageQuery(null, null, null, null);
        UserAccount user = new UserAccount();
        user.setId(1L);
        when(userAccountRepository.listByCondition(1L, null, null, null, null, 0L, 10))
                .thenReturn(List.of(user));

        List<UserAccount> result = userManagementService.listUsers(1L, query, 0L, 10);
        assertThat(result).hasSize(1);
    }

    @Test
    void grantRoleCreatesRelationship() {
        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setTenantId(1L);
        Role role = new Role();
        role.setId(20L);
        role.setRoleCode("DOCTOR");

        when(userAccountRepository.findById(1L, 10L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L, 20L)).thenReturn(Optional.of(role));
        when(userRoleRelRepository.findByUserIdAndRoleId(1L, 10L, 20L)).thenReturn(Optional.empty());
        when(userRoleRelRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new IdentityApiDtos.Request.GrantRoleRequest(10L, 20L, 99L);
        UserAccount result = userManagementService.grantRole(1L, 10L, request);

        assertThat(result.getId()).isEqualTo(10L);

        ArgumentCaptor<UserRoleRel> captor = ArgumentCaptor.forClass(UserRoleRel.class);
        verify(userRoleRelRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getRoleId()).isEqualTo(20L);
        assertThat(captor.getValue().getGrantedBy()).isEqualTo(99L);
    }

    @Test
    void grantRoleIsIdempotent() {
        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setTenantId(1L);
        Role role = new Role();
        role.setId(20L);
        UserRoleRel existing = new UserRoleRel();
        existing.setUserId(10L);
        existing.setRoleId(20L);

        when(userAccountRepository.findById(1L, 10L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L, 20L)).thenReturn(Optional.of(role));
        when(userRoleRelRepository.findByUserIdAndRoleId(1L, 10L, 20L))
                .thenReturn(Optional.of(existing));

        var request = new IdentityApiDtos.Request.GrantRoleRequest(10L, 20L, 99L);
        userManagementService.grantRole(1L, 10L, request);

        verify(userRoleRelRepository, never()).save(any());
    }

    @Test
    void grantRoleThrowsWhenUserNotFound() {
        when(userAccountRepository.findById(1L, 999L)).thenReturn(Optional.empty());

        var request = new IdentityApiDtos.Request.GrantRoleRequest(999L, 20L, 99L);
        assertThatThrownBy(() -> userManagementService.grantRole(1L, 999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void grantRoleThrowsWhenRoleNotFound() {
        UserAccount user = new UserAccount();
        user.setId(10L);
        when(userAccountRepository.findById(1L, 10L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L, 999L)).thenReturn(Optional.empty());

        var request = new IdentityApiDtos.Request.GrantRoleRequest(10L, 999L, 99L);
        assertThatThrownBy(() -> userManagementService.grantRole(1L, 10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
    }
}
