package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.audit.internal.service.AuditTrailService;
import xenosoft.imldintelligence.module.audit.internal.service.command.AuditRecordCommand;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.security.CurrentUserSubjectProvider;
import xenosoft.imldintelligence.module.identity.internal.service.AuthService;
import xenosoft.imldintelligence.module.identity.internal.service.PermissionService;
import xenosoft.imldintelligence.module.identity.internal.service.SensitiveDataEncryptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountSettingsServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PermissionService permissionService;
    @Mock
    private SensitiveDataEncryptor sensitiveDataEncryptor;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthService authService;
    @Mock
    private AuditTrailService auditTrailService;

    private AccountSettingsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountSettingsServiceImpl(
                new CurrentUserSubjectProvider(),
                userAccountRepository,
                permissionService,
                sensitiveDataEncryptor,
                passwordEncoder,
                authService,
                Optional.of(auditTrailService)
        );
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        new UserSubject(100L, 1L, "DOCTOR", "Hepatology", Set.of("DOCTOR")),
                        "access-token",
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetCurrentProfile() {
        UserAccount user = activeUser();
        user.setMobileEncrypted("encrypted-mobile");
        when(userAccountRepository.findById(1L, 100L)).thenReturn(Optional.of(user));
        when(permissionService.getEffectiveRoleCodes(1L, 100L)).thenReturn(Set.of("DOCTOR"));
        when(sensitiveDataEncryptor.decrypt("encrypted-mobile")).thenReturn("13800138000");
        when(sensitiveDataEncryptor.mask("13800138000", SensitiveDataEncryptor.MaskType.MOBILE))
                .thenReturn("138****8000");

        IdentityApiDtos.Response.AccountProfileResponse response = service.getCurrentProfile(1L);

        assertThat(response.userId()).isEqualTo(100L);
        assertThat(response.username()).isEqualTo("doctor01");
        assertThat(response.mobileMasked()).isEqualTo("138****8000");
        assertThat(response.roleCodes()).containsExactly("DOCTOR");
    }

    @Test
    void shouldUpdateDisplayNameAndDepartment() {
        UserAccount user = activeUser();
        when(userAccountRepository.findById(1L, 100L)).thenReturn(Optional.of(user));
        when(userAccountRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionService.getEffectiveRoleCodes(1L, 100L)).thenReturn(Set.of("DOCTOR"));

        IdentityApiDtos.Response.AccountProfileResponse response = service.updateCurrentProfile(
                1L,
                new IdentityApiDtos.Request.UpdateAccountProfileCommand(
                        "Doctor Ren",
                        "Liver Center",
                        null,
                        null,
                        null
                )
        );

        assertThat(response.displayName()).isEqualTo("Doctor Ren");
        assertThat(response.deptName()).isEqualTo("Liver Center");
        verify(userAccountRepository).update(user);

        ArgumentCaptor<AuditRecordCommand> auditCaptor = ArgumentCaptor.forClass(AuditRecordCommand.class);
        verify(auditTrailService).recordAudit(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo("ACCOUNT_PROFILE_UPDATE");
        assertThat(auditCaptor.getValue().getBeforeData()).isNull();
        assertThat(auditCaptor.getValue().getAfterData()).isNull();
    }

    @Test
    void shouldRejectContactChangeWithoutCurrentPassword() {
        UserAccount user = activeUser();
        when(userAccountRepository.findById(1L, 100L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.updateCurrentProfile(
                1L,
                new IdentityApiDtos.Request.UpdateAccountProfileCommand(
                        null,
                        null,
                        "new@example.com",
                        null,
                        null
                )
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(userAccountRepository, never()).update(any());
    }

    @Test
    void shouldRejectContactChangeWhenCurrentPasswordIsWrong() {
        UserAccount user = activeUser();
        when(userAccountRepository.findById(1L, 100L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-old")).thenReturn(false);

        assertThatThrownBy(() -> service.updateCurrentProfile(
                1L,
                new IdentityApiDtos.Request.UpdateAccountProfileCommand(
                        null,
                        null,
                        "new@example.com",
                        null,
                        "wrong"
                )
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userAccountRepository, never()).update(any());
    }

    @Test
    void shouldRejectDuplicateEmailWithinTenant() {
        UserAccount user = activeUser();
        UserAccount duplicate = activeUser();
        duplicate.setId(101L);
        duplicate.setEmail("new@example.com");
        when(userAccountRepository.findById(1L, 100L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);
        when(userAccountRepository.findByEmail(1L, "new@example.com")).thenReturn(Optional.of(duplicate));

        assertThatThrownBy(() -> service.updateCurrentProfile(
                1L,
                new IdentityApiDtos.Request.UpdateAccountProfileCommand(
                        null,
                        null,
                        "new@example.com",
                        null,
                        "old-password"
                )
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userAccountRepository, never()).update(any());
    }

    @Test
    void shouldChangePasswordAndRevokeRefreshAndAccessTokens() {
        UserAccount user = activeUser();
        when(userAccountRepository.findById(1L, 100L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("NewPass_123")).thenReturn("encoded-new");
        when(userAccountRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionService.getEffectiveRoleCodes(1L, 100L)).thenReturn(Set.of("DOCTOR"));

        service.changeCurrentPassword(
                1L,
                new IdentityApiDtos.Request.ChangePasswordCommand(
                        "old-password",
                        "NewPass_123",
                        "NewPass_123",
                        "refresh-token"
                ),
                "Bearer access-token"
        );

        assertThat(user.getPasswordHash()).isEqualTo("encoded-new");
        verify(authService).revokeToken("refresh-token");
        verify(authService).revokeToken("access-token");

        ArgumentCaptor<AuditRecordCommand> auditCaptor = ArgumentCaptor.forClass(AuditRecordCommand.class);
        verify(auditTrailService).recordAudit(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo("ACCOUNT_PASSWORD_CHANGE");
        assertThat(auditCaptor.getValue().getBeforeData()).isNull();
        assertThat(auditCaptor.getValue().getAfterData()).isNull();
    }

    private UserAccount activeUser() {
        UserAccount user = new UserAccount();
        user.setId(100L);
        user.setTenantId(1L);
        user.setUserNo("U-100");
        user.setUsername("doctor01");
        user.setPasswordHash("encoded-old");
        user.setDisplayName("Doctor One");
        user.setUserType("DOCTOR");
        user.setDeptName("Hepatology");
        user.setEmail("doctor01@example.com");
        user.setStatus("ACTIVE");
        return user;
    }
}
