package xenosoft.imldintelligence.module.identity.internal.service.impl;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.dto.LoginRequest;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.security.IdentitySecurityProperties;
import xenosoft.imldintelligence.module.identity.internal.security.RefreshTokenSubject;
import xenosoft.imldintelligence.module.identity.internal.service.PermissionService;
import xenosoft.imldintelligence.module.identity.internal.service.TokenBlacklistService;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
class AuthServiceImplTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private PermissionService permissionService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenBlacklistService tokenBlacklistService;

    private JwtUtil jwtUtil;
    private AuthServiceImpl authService;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-07T00:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        IdentitySecurityProperties properties = new IdentitySecurityProperties();
        properties.getJwt().setIssuer("imld-test");
        properties.getJwt().setSecret("01234567890123456789012345678901");
        properties.getJwt().setAccessTokenTtl(Duration.ofMinutes(15));
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(7));
        properties.getJwt().setClockSkew(Duration.ZERO);

        jwtUtil = new JwtUtil(properties, FIXED_CLOCK);
        authService = new AuthServiceImpl(
                userAccountRepository, tenantRepository, permissionService,
                jwtUtil, passwordEncoder, tokenBlacklistService);
    }

    @Test
    void loginSuccessful() {
        Tenant tenant = tenant(1L, "HOSP_A");
        UserAccount user = activeUser(1L, 10L, "doctor1", "hashed_pw");

        when(tenantRepository.findByTenantCode("HOSP_A")).thenReturn(Optional.of(tenant));
        when(userAccountRepository.findByUsername(1L, "doctor1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed_pw")).thenReturn(true);
        when(permissionService.getEffectiveRoleCodes(1L, 10L)).thenReturn(Set.of("DOCTOR"));
        when(userAccountRepository.update(any())).thenReturn(user);

        AuthToken token = authService.login(new LoginRequest("doctor1", "password123", "HOSP_A"));

        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.refreshToken()).isNotBlank();
        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresIn()).isEqualTo(900L);
        verify(userAccountRepository).update(any());
    }

    @Test
    void loginFailsWithWrongPassword() {
        Tenant tenant = tenant(1L, "HOSP_A");
        UserAccount user = activeUser(1L, 10L, "doctor1", "hashed_pw");

        when(tenantRepository.findByTenantCode("HOSP_A")).thenReturn(Optional.of(tenant));
        when(userAccountRepository.findByUsername(1L, "doctor1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed_pw")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("doctor1", "wrong", "HOSP_A")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void loginFailsWithUnknownUser() {
        Tenant tenant = tenant(1L, "HOSP_A");
        when(tenantRepository.findByTenantCode("HOSP_A")).thenReturn(Optional.of(tenant));
        when(userAccountRepository.findByUsername(1L, "nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody", "pass", "HOSP_A")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void loginFailsWithUnknownTenant() {
        when(tenantRepository.findByTenantCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("user", "pass", "UNKNOWN")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    void loginUsesDefaultTenantWhenCodeIsNull() {
        Tenant tenant = tenant(1L, "DEFAULT");
        UserAccount user = activeUser(1L, 10L, "admin", "hashed");

        when(tenantRepository.listAll()).thenReturn(List.of(tenant));
        when(userAccountRepository.findByUsername(1L, "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(permissionService.getEffectiveRoleCodes(1L, 10L)).thenReturn(Set.of("SYSTEM_ADMIN"));
        when(userAccountRepository.update(any())).thenReturn(user);

        AuthToken token = authService.login(new LoginRequest("admin", "pass", null));

        assertThat(token.accessToken()).isNotBlank();
    }

    @Test
    void refreshTokenSuccessful() {
        String refreshToken = jwtUtil.generateRefreshToken(new RefreshTokenSubject(10L, 1L));
        UserSubject subject = new UserSubject(10L, 1L, "DOCTOR", "ICU", Set.of("DOCTOR"));

        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);
        when(permissionService.loadSubject(1L, 10L)).thenReturn(subject);

        AuthToken result = authService.refreshToken(refreshToken);

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void refreshTokenFailsWhenBlacklisted() {
        String refreshToken = jwtUtil.generateRefreshToken(new RefreshTokenSubject(10L, 1L));
        String jti = jwtUtil.extractJti(refreshToken);

        when(tokenBlacklistService.isBlacklisted(jti)).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void refreshTokenFailsWhenUserInactive() {
        String refreshToken = jwtUtil.generateRefreshToken(new RefreshTokenSubject(10L, 1L));

        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);
        when(permissionService.loadSubject(1L, 10L)).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void validateTokenReturnsTrueForValid() {
        UserSubject subject = new UserSubject(10L, 1L, "DOCTOR", null, Set.of("DOCTOR"));
        String token = jwtUtil.generateAccessToken(subject);

        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);

        assertThat(authService.validateToken(token)).isTrue();
    }

    @Test
    void validateTokenReturnsFalseWhenBlacklisted() {
        UserSubject subject = new UserSubject(10L, 1L, "DOCTOR", null, Set.of("DOCTOR"));
        String token = jwtUtil.generateAccessToken(subject);
        String jti = jwtUtil.extractJti(token);

        when(tokenBlacklistService.isBlacklisted(jti)).thenReturn(true);

        assertThat(authService.validateToken(token)).isFalse();
    }

    @Test
    void validateTokenReturnsFalseForInvalidToken() {
        assertThat(authService.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void revokeTokenBlacklistsJti() {
        UserSubject subject = new UserSubject(10L, 1L, "DOCTOR", null, Set.of("DOCTOR"));
        String token = jwtUtil.generateAccessToken(subject);

        authService.revokeToken(token);

        verify(tokenBlacklistService).blacklist(anyString(), any(Duration.class));
    }

    @Test
    void logoutBlacklistsRefreshToken() {
        String refreshToken = jwtUtil.generateRefreshToken(new RefreshTokenSubject(10L, 1L));

        authService.logout(refreshToken);

        verify(tokenBlacklistService).blacklist(anyString(), any(Duration.class));
    }

    @Test
    void logoutIgnoresNullToken() {
        authService.logout(null);
        verify(tokenBlacklistService, never()).blacklist(anyString(), any());
    }

    @Test
    void generateTokenFromRole() {
        Role role = new Role();
        role.setTenantId(1L);
        role.setRoleCode("SYSTEM_ADMIN");

        AuthToken token = authService.generateToken(role);

        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.refreshToken()).isNotBlank();
    }

    private Tenant tenant(Long id, String code) {
        Tenant t = new Tenant();
        t.setId(id);
        t.setTenantCode(code);
        t.setStatus("ACTIVE");
        return t;
    }

    private UserAccount activeUser(Long tenantId, Long userId, String username, String passwordHash) {
        UserAccount u = new UserAccount();
        u.setId(userId);
        u.setTenantId(tenantId);
        u.setUsername(username);
        u.setPasswordHash(passwordHash);
        u.setUserType("DOCTOR");
        u.setDeptName("ICU");
        u.setStatus("ACTIVE");
        return u;
    }
}
