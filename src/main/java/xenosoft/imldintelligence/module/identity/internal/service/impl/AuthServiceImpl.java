package xenosoft.imldintelligence.module.identity.internal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.dto.LoginRequest;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.security.RefreshTokenSubject;
import xenosoft.imldintelligence.module.identity.internal.service.AuthService;
import xenosoft.imldintelligence.module.identity.internal.service.PermissionService;
import xenosoft.imldintelligence.module.identity.internal.service.TokenBlacklistService;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final TenantRepository tenantRepository;
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public AuthToken login(LoginRequest loginRequest) {
        Tenant tenant = resolveTenant(loginRequest.tenantCode());

        UserAccount user = userAccountRepository
                .findByUsername(tenant.getId(), loginRequest.username())
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        Set<String> roleCodes = permissionService.getEffectiveRoleCodes(tenant.getId(), user.getId());

        UserSubject subject = new UserSubject(
                user.getId(),
                tenant.getId(),
                user.getUserType(),
                user.getDeptName(),
                roleCodes
        );

        String accessToken = jwtUtil.generateAccessToken(subject);
        String refreshToken = jwtUtil.generateRefreshToken(
                new RefreshTokenSubject(user.getId(), tenant.getId()));

        user.setLastLoginAt(OffsetDateTime.now());
        userAccountRepository.update(user);

        return AuthToken.bearer(accessToken, refreshToken, jwtUtil.getAccessTokenExpiresInSeconds());
    }

    @Override
    public AuthToken refreshToken(String token) {
        RefreshTokenSubject refreshSubject = jwtUtil.parseRefreshToken(token);

        String jti = jwtUtil.extractJti(token);
        if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        UserSubject subject = permissionService.loadSubject(
                refreshSubject.tenantId(), refreshSubject.userId());
        if (subject == null) {
            throw new IllegalArgumentException("User not found or inactive");
        }

        String accessToken = jwtUtil.generateAccessToken(subject);

        return AuthToken.bearer(accessToken, token, jwtUtil.getAccessTokenExpiresInSeconds());
    }

    @Override
    public boolean validateToken(String token) {
        if (!jwtUtil.isAccessTokenValid(token)) {
            return false;
        }
        String jti = jwtUtil.extractJti(token);
        return jti == null || !tokenBlacklistService.isBlacklisted(jti);
    }

    @Override
    public void revokeToken(String token) {
        blacklistToken(token);
    }

    @Override
    public void logout(String refreshToken) {
        blacklistToken(refreshToken);
    }

    @Override
    public AuthToken generateToken(Role role) {
        if (role == null || role.getTenantId() == null) {
            throw new IllegalArgumentException("Role with tenantId is required");
        }
        UserSubject subject = new UserSubject(
                0L,
                role.getTenantId(),
                "SYSTEM",
                null,
                Set.of(role.getRoleCode())
        );
        String accessToken = jwtUtil.generateAccessToken(subject);
        String refreshToken = jwtUtil.generateRefreshToken(
                new RefreshTokenSubject(0L, role.getTenantId()));
        return AuthToken.bearer(accessToken, refreshToken, jwtUtil.getAccessTokenExpiresInSeconds());
    }

    private Tenant resolveTenant(String tenantCode) {
        if (tenantCode != null && !tenantCode.isBlank()) {
            return tenantRepository.findByTenantCode(tenantCode)
                    .filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus()))
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantCode));
        }
        // Default: use the first active tenant
        return tenantRepository.listAll().stream()
                .filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active tenant available"));
    }

    private void blacklistToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String jti = jwtUtil.extractJti(token);
        if (jti == null) {
            return;
        }
        Duration ttl = jwtUtil.getRemainingTtl(token);
        if (!ttl.isZero()) {
            tokenBlacklistService.blacklist(jti, ttl);
        }
    }
}
