package xenosoft.imldintelligence.module.identity.internal.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.security.IdentitySecurityProperties;
import xenosoft.imldintelligence.module.identity.internal.security.RefreshTokenSubject;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {
    private IdentitySecurityProperties properties;

    @BeforeEach
    void setUp() {
        properties = new IdentitySecurityProperties();
        properties.getJwt().setIssuer("imld-test");
        properties.getJwt().setSecret("01234567890123456789012345678901");
        properties.getJwt().setAccessTokenTtl(Duration.ofMinutes(10));
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(7));
        properties.getJwt().setClockSkew(Duration.ZERO);
    }

    @Test
    void shouldGenerateAndParseAccessToken() {
        JwtUtil jwtUtil = new JwtUtil(properties, Clock.fixed(Instant.parse("2026-03-07T00:00:00Z"), ZoneOffset.UTC));
        UserSubject subject = new UserSubject(101L, 202L, "DOCTOR", "Cardiology", Set.of("SYSTEM_ADMIN", "DOCTOR"));

        String token = jwtUtil.generateAccessToken(subject);
        UserSubject parsed = jwtUtil.parseAccessToken(token);

        assertThat(token).isNotBlank();
        assertThat(parsed.userId()).isEqualTo(101L);
        assertThat(parsed.tenantId()).isEqualTo(202L);
        assertThat(parsed.userType()).isEqualTo("DOCTOR");
        assertThat(parsed.deptName()).isEqualTo("Cardiology");
        assertThat(parsed.roleCodes()).containsExactlyInAnyOrder("SYSTEM_ADMIN", "DOCTOR");
        assertThat(jwtUtil.isAccessTokenValid(token)).isTrue();
    }

    @Test
    void shouldGenerateAndParseRefreshToken() {
        JwtUtil jwtUtil = new JwtUtil(properties, Clock.fixed(Instant.parse("2026-03-07T00:00:00Z"), ZoneOffset.UTC));

        String token = jwtUtil.generateRefreshToken(new RefreshTokenSubject(301L, 401L));
        RefreshTokenSubject parsed = jwtUtil.parseRefreshToken(token);

        assertThat(parsed.userId()).isEqualTo(301L);
        assertThat(parsed.tenantId()).isEqualTo(401L);
        assertThat(jwtUtil.isRefreshTokenValid(token)).isTrue();
    }

    @Test
    void shouldRejectExpiredAccessToken() {
        Clock issuedClock = Clock.fixed(Instant.parse("2026-03-07T00:00:00Z"), ZoneOffset.UTC);
        JwtUtil issuingJwtUtil = new JwtUtil(properties, issuedClock);
        String token = issuingJwtUtil.generateAccessToken(new UserSubject(101L, 202L, "DOCTOR", "Cardiology", Set.of("DOCTOR")));

        JwtUtil expiredJwtUtil = new JwtUtil(properties, Clock.offset(issuedClock, Duration.ofMinutes(11)));

        assertThatThrownBy(() -> expiredJwtUtil.parseAccessToken(token))
                .isInstanceOf(ExpiredJwtException.class);
        assertThat(expiredJwtUtil.isAccessTokenValid(token)).isFalse();
    }

    @Test
    void shouldRejectRefreshTokenWhenParsedAsAccessToken() {
        JwtUtil jwtUtil = new JwtUtil(properties, Clock.fixed(Instant.parse("2026-03-07T00:00:00Z"), ZoneOffset.UTC));
        String refreshToken = jwtUtil.generateRefreshToken(new RefreshTokenSubject(301L, 401L));

        assertThatThrownBy(() -> jwtUtil.parseAccessToken(refreshToken))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Unexpected JWT token type");
    }
}
