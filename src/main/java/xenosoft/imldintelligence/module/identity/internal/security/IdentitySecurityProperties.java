package xenosoft.imldintelligence.module.identity.internal.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 身份安全配置属性，定义安全开关、公开路径以及 JWT 相关参数。
 */
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

    /**
     * 在启用安全能力时校验 JWT 配置是否完整可用。
     *
     * @throws IllegalStateException JWT 配置缺失或不合法时抛出
     */
    public void validateWhenSecurityEnabled() {
        if (!enabled) {
            return;
        }
        validateJwtConfiguration();
    }

    /**
     * 校验 JWT 发行者、密钥长度、过期时间与时钟偏移配置。
     *
     * @throws IllegalStateException JWT 配置缺失或不符合安全要求时抛出
     */
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

    /**
     * JWT 参数配置，定义签发者、密钥、访问令牌时长、刷新令牌时长与时钟偏移。
     */
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
