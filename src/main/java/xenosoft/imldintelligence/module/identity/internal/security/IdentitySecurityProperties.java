package xenosoft.imldintelligence.module.identity.internal.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "imld.security")
public class IdentitySecurityProperties {
    private boolean enabled = false;
    private List<String> publicPaths = new ArrayList<>(List.of(
            "/error",
            "/api/v1/auth/**",
            "/actuator/health",
            "/actuator/info"
    ));
    private Jwt jwt = new Jwt();

    public void validateWhenSecurityEnabled() {
        if (!enabled) {
            return;
        }
        validateJwtConfiguration();
    }

    public void validateJwtConfiguration() {
        if (jwt.getIssuer() == null || jwt.getIssuer().isBlank()) {
            throw new IllegalStateException("JWT issuer must not be blank.");
        }
        if (jwt.getSecret() == null || jwt.getSecret().isBlank()) {
            throw new IllegalStateException("JWT secret must be configured when JWT support is used.");
        }
        if (jwt.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256.");
        }
        if (jwt.getAccessTokenTtl() == null || jwt.getAccessTokenTtl().isNegative() || jwt.getAccessTokenTtl().isZero()) {
            throw new IllegalStateException("JWT access token TTL must be greater than zero.");
        }
        if (jwt.getRefreshTokenTtl() == null || jwt.getRefreshTokenTtl().isNegative() || jwt.getRefreshTokenTtl().isZero()) {
            throw new IllegalStateException("JWT refresh token TTL must be greater than zero.");
        }
        if (jwt.getClockSkew() == null || jwt.getClockSkew().isNegative()) {
            throw new IllegalStateException("JWT clock skew must not be negative.");
        }
    }

    @Getter
    @Setter
    public static class Jwt {
        private String issuer = "imld-intelligence";
        private String secret = "";
        private Duration accessTokenTtl = Duration.ofMinutes(15);
        private Duration refreshTokenTtl = Duration.ofDays(7);
        private Duration clockSkew = Duration.ofSeconds(30);
    }
}
