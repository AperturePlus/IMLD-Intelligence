package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.config.IdentityVerificationProperties;
import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.model.EmailVerificationCode;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.EmailVerificationCodeRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;
import xenosoft.imldintelligence.module.identity.internal.service.AuthService;
import xenosoft.imldintelligence.module.identity.internal.service.VerificationEmailSender;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountCredentialServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private UserRoleRelRepository userRoleRelRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmailVerificationCodeRepository emailVerificationCodeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthService authService;
    @Mock
    private VerificationEmailSender verificationEmailSender;

    private AccountCredentialServiceImpl service;

    @BeforeEach
    void setUp() {
        IdentityVerificationProperties properties = new IdentityVerificationProperties();
        properties.setCodeLength(6);
        properties.setMaxVerifyAttempts(5);
        properties.setExpiresIn(java.time.Duration.ofMinutes(10));
        properties.setResendInterval(java.time.Duration.ofSeconds(60));
        service = new AccountCredentialServiceImpl(
                tenantRepository,
                userAccountRepository,
                userRoleRelRepository,
                roleRepository,
                emailVerificationCodeRepository,
                passwordEncoder,
                authService,
                verificationEmailSender,
                properties
        );
    }

    @Test
    void shouldSendRegistrationEmailCode() {
        Tenant tenant = activeTenant();
        when(tenantRepository.findByTenantCode("saas")).thenReturn(Optional.of(tenant));
        when(userAccountRepository.findByUsername(1L, "doctor01")).thenReturn(Optional.empty());
        when(userAccountRepository.findByEmail(1L, "doctor01@example.com")).thenReturn(Optional.empty());
        when(emailVerificationCodeRepository.findLatestByCondition(1L, "REGISTER", "doctor01@example.com", "doctor01"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-code");

        IdentityApiDtos.Response.EmailCodeSendResponse response = service.sendRegistrationEmailCode(
                new IdentityApiDtos.Request.SendRegistrationEmailCodeCommand(
                        "doctor01",
                        "doctor01@example.com",
                        "saas"
                )
        );

        assertThat(response.purpose()).isEqualTo("REGISTER");
        assertThat(response.email()).isEqualTo("doctor01@example.com");

        ArgumentCaptor<EmailVerificationCode> captor = ArgumentCaptor.forClass(EmailVerificationCode.class);
        verify(emailVerificationCodeRepository).save(captor.capture());
        assertThat(captor.getValue().getPurpose()).isEqualTo("REGISTER");
        assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");

        verify(verificationEmailSender).sendVerificationCode(
                eq("doctor01@example.com"),
                eq("registration"),
                anyString(),
                any()
        );
    }

    @Test
    void shouldRegisterWhenCodeIsValid() {
        Tenant tenant = activeTenant();
        when(tenantRepository.findByTenantCode("saas")).thenReturn(Optional.of(tenant));
        when(userAccountRepository.findByUsername(1L, "doctor02")).thenReturn(Optional.empty());
        when(userAccountRepository.findByEmail(1L, "doctor02@example.com")).thenReturn(Optional.empty());
        when(userAccountRepository.findByUserNo(anyLong(), anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleCode(anyLong(), anyString())).thenReturn(Optional.empty());

        EmailVerificationCode code = new EmailVerificationCode();
        code.setId(10L);
        code.setTenantId(1L);
        code.setUsername("doctor02");
        code.setEmail("doctor02@example.com");
        code.setPurpose("REGISTER");
        code.setCodeHash("encoded-code");
        code.setVerifyAttemptCount(0);
        code.setMaxVerifyAttempts(5);
        code.setStatus("PENDING");
        code.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5));
        when(emailVerificationCodeRepository.findLatestPending(
                eq(1L), eq("REGISTER"), eq("doctor02@example.com"), eq("doctor02"), any())
        ).thenReturn(Optional.of(code));
        when(emailVerificationCodeRepository.consumePendingCode(eq(1L), eq(10L), any())).thenReturn(true);

        when(passwordEncoder.matches("123456", "encoded-code")).thenReturn(true);
        when(passwordEncoder.encode("StrongPass_123")).thenReturn("encoded-password");
        when(userAccountRepository.save(any())).thenAnswer(invocation -> {
            UserAccount user = invocation.getArgument(0);
            user.setId(200L);
            return user;
        });
        when(authService.login(any())).thenReturn(AuthToken.bearer("access-token", "refresh-token", 900L));

        AuthToken token = service.register(new IdentityApiDtos.Request.RegisterCommand(
                "doctor02",
                "StrongPass_123",
                "doctor02@example.com",
                "123456",
                "Doctor Two",
                "DOCTOR",
                "saas"
        ));

        assertThat(token.accessToken()).isEqualTo("access-token");
        verify(emailVerificationCodeRepository).consumePendingCode(eq(1L), eq(10L), any());
    }

    @Test
    void shouldRejectResetPasswordWhenCodeInvalid() {
        Tenant tenant = activeTenant();
        when(tenantRepository.findByTenantCode("saas")).thenReturn(Optional.of(tenant));

        UserAccount user = new UserAccount();
        user.setId(300L);
        user.setTenantId(1L);
        user.setUsername("doctor03");
        user.setEmail("doctor03@example.com");
        user.setStatus("ACTIVE");
        when(userAccountRepository.findByUsername(1L, "doctor03")).thenReturn(Optional.of(user));

        EmailVerificationCode code = new EmailVerificationCode();
        code.setId(20L);
        code.setTenantId(1L);
        code.setUsername("doctor03");
        code.setEmail("doctor03@example.com");
        code.setPurpose("PASSWORD_RESET");
        code.setCodeHash("encoded-code");
        code.setVerifyAttemptCount(0);
        code.setMaxVerifyAttempts(5);
        code.setStatus("PENDING");
        code.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5));
        when(emailVerificationCodeRepository.findLatestPending(
                eq(1L), eq("PASSWORD_RESET"), eq("doctor03@example.com"), eq("doctor03"), any())
        ).thenReturn(Optional.of(code));
        when(passwordEncoder.matches("000000", "encoded-code")).thenReturn(false);

        assertThatThrownBy(() -> service.resetPassword(new IdentityApiDtos.Request.ResetPasswordCommand(
                "doctor03",
                "doctor03@example.com",
                "NewPass_456",
                "000000",
                "saas"
        )))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Verification code is incorrect");

        verify(emailVerificationCodeRepository).incrementVerifyAttempt(eq(1L), eq(20L), eq(1), eq("PENDING"), any());
        verify(userAccountRepository, never()).update(any());
    }

    private Tenant activeTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setTenantCode("saas");
        tenant.setStatus("ACTIVE");
        return tenant;
    }
}
