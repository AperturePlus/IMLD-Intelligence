package xenosoft.imldintelligence.module.identity.internal.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.security.IdentitySecurityProperties;
import xenosoft.imldintelligence.module.identity.internal.security.JwtTokenType;
import xenosoft.imldintelligence.module.identity.internal.security.RefreshTokenSubject;
import xenosoft.imldintelligence.module.identity.internal.security.RoleAuthorityUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT 工具类，负责生成、解析与校验访问令牌和刷新令牌。
 */
@Component
public class JwtUtil {
    static final String CLAIM_USER_ID = "uid";
    static final String CLAIM_TENANT_ID = "tenant_id";
    static final String CLAIM_USER_TYPE = "user_type";
    static final String CLAIM_DEPT_NAME = "dept_name";
    static final String CLAIM_ROLE_CODES = "role_codes";
    static final String CLAIM_TOKEN_TYPE = "token_type";

    private final IdentitySecurityProperties properties;
    private final Clock clock;

    @Autowired
    public JwtUtil(IdentitySecurityProperties properties) {
        this(properties, Clock.systemUTC());
    }

    public JwtUtil(IdentitySecurityProperties properties, Clock clock) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * 根据用户主体生成访问令牌。
     *
     * @param userSubject 当前登录用户的主体信息
     * @return 签发后的访问令牌
     */
    public String generateAccessToken(UserSubject userSubject) {
        Objects.requireNonNull(userSubject, "userSubject must not be null");

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(CLAIM_USER_ID, userSubject.userId());
        claims.put(CLAIM_TENANT_ID, userSubject.tenantId());
        claims.put(CLAIM_USER_TYPE, requireText(userSubject.userType(), "userType"));
        claims.put(CLAIM_DEPT_NAME, trimToNull(userSubject.deptName()));
        claims.put(CLAIM_ROLE_CODES, List.copyOf(RoleAuthorityUtils.normalizeRoleCodes(userSubject.roleCodes())));

        return buildToken(JwtTokenType.ACCESS, claims, String.valueOf(userSubject.userId()), properties.getJwt().getAccessTokenTtl());
    }

    /**
     * 根据刷新主体生成刷新令牌。
     *
     * @param refreshTokenSubject 刷新令牌主体信息
     * @return 签发后的刷新令牌
     */
    public String generateRefreshToken(RefreshTokenSubject refreshTokenSubject) {
        Objects.requireNonNull(refreshTokenSubject, "refreshTokenSubject must not be null");

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(CLAIM_USER_ID, refreshTokenSubject.userId());
        claims.put(CLAIM_TENANT_ID, refreshTokenSubject.tenantId());

        return buildToken(JwtTokenType.REFRESH, claims, String.valueOf(refreshTokenSubject.userId()), properties.getJwt().getRefreshTokenTtl());
    }

    /**
     * 解析访问令牌并恢复用户主体信息。
     *
     * @param token 待解析的访问令牌
     * @return 访问令牌对应的用户主体信息
     * @throws JwtException 令牌格式错误、已过期或类型不匹配时抛出
     */
    public UserSubject parseAccessToken(String token) {
        Claims claims = parseClaims(token, JwtTokenType.ACCESS);
        return new UserSubject(
                parseRequiredLong(claims, CLAIM_USER_ID),
                parseRequiredLong(claims, CLAIM_TENANT_ID),
                parseRequiredString(claims, CLAIM_USER_TYPE),
                trimToNull(claims.get(CLAIM_DEPT_NAME, String.class)),
                parseRoleCodes(claims)
        );
    }

    /**
     * 解析刷新令牌并恢复刷新主体信息。
     *
     * @param token 待解析的刷新令牌
     * @return 刷新令牌对应的主体信息
     * @throws JwtException 令牌格式错误、已过期或类型不匹配时抛出
     */
    public RefreshTokenSubject parseRefreshToken(String token) {
        Claims claims = parseClaims(token, JwtTokenType.REFRESH);
        return new RefreshTokenSubject(
                parseRequiredLong(claims, CLAIM_USER_ID),
                parseRequiredLong(claims, CLAIM_TENANT_ID)
        );
    }

    /**
     * 判断访问令牌是否有效。
     *
     * @param token 待校验的访问令牌
     * @return 访问令牌有效时返回 {@code true}，否则返回 {@code false}
     */
    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, JwtTokenType.ACCESS);
    }

    /**
     * 判断刷新令牌是否有效。
     *
     * @param token 待校验的刷新令牌
     * @return 刷新令牌有效时返回 {@code true}，否则返回 {@code false}
     */
    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, JwtTokenType.REFRESH);
    }

    /**
     * 从令牌中提取 JTI（JWT ID）。
     *
     * @param token 待解析的令牌
     * @return JTI 字符串，解析失败时返回 {@code null}
     */
    public String extractJti(String token) {
        try {
            properties.validateJwtConfiguration();
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .requireIssuer(properties.getJwt().getIssuer())
                    .clock(() -> Date.from(Instant.now(clock)))
                    .clockSkewSeconds(properties.getJwt().getClockSkew().toSeconds())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getId();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * 从令牌中提取剩余有效期。
     *
     * @param token 待解析的令牌
     * @return 剩余有效期，解析失败或已过期时返回 {@code Duration.ZERO}
     */
    public Duration getRemainingTtl(String token) {
        try {
            properties.validateJwtConfiguration();
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .requireIssuer(properties.getJwt().getIssuer())
                    .clock(() -> Date.from(Instant.now(clock)))
                    .clockSkewSeconds(properties.getJwt().getClockSkew().toSeconds())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return Duration.ZERO;
            }
            Duration remaining = Duration.between(Instant.now(clock), expiration.toInstant());
            return remaining.isNegative() ? Duration.ZERO : remaining;
        } catch (JwtException | IllegalArgumentException ex) {
            return Duration.ZERO;
        }
    }

    /**
     * 返回访问令牌的有效期秒数。
     *
     * @return 访问令牌默认有效期对应的秒数
     */
    public long getAccessTokenExpiresInSeconds() {
        properties.validateJwtConfiguration();
        return properties.getJwt().getAccessTokenTtl().toSeconds();
    }

    private String buildToken(JwtTokenType tokenType,
                              Map<String, Object> claims,
                              String subject,
                              Duration ttl) {
        properties.validateJwtConfiguration();

        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(ttl);

        return Jwts.builder()
                .issuer(properties.getJwt().getIssuer())
                .subject(subject)
                .claims(claims)
                .claim(CLAIM_TOKEN_TYPE, tokenType.claimValue())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    private Claims parseClaims(String token, JwtTokenType expectedTokenType) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT must not be blank");
        }
        properties.validateJwtConfiguration();

        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .requireIssuer(properties.getJwt().getIssuer())
                .clock(() -> Date.from(Instant.now(clock)))
                .clockSkewSeconds(properties.getJwt().getClockSkew().toSeconds())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        JwtTokenType actualTokenType = JwtTokenType.fromClaim(claims.get(CLAIM_TOKEN_TYPE, String.class));
        if (actualTokenType != expectedTokenType) {
            throw new JwtException("Unexpected JWT token type: " + actualTokenType);
        }
        return claims;
    }

    private boolean isTokenValid(String token, JwtTokenType tokenType) {
        try {
            parseClaims(token, tokenType);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private long parseRequiredLong(Claims claims, String claimName) {
        Number value = claims.get(claimName, Number.class);
        if (value == null || value.longValue() <= 0) {
            throw new JwtException("JWT claim '" + claimName + "' is missing or invalid");
        }
        return value.longValue();
    }

    private String parseRequiredString(Claims claims, String claimName) {
        String value = claims.get(claimName, String.class);
        if (value == null || value.isBlank()) {
            throw new JwtException("JWT claim '" + claimName + "' is missing or blank");
        }
        return value;
    }

    private Set<String> parseRoleCodes(Claims claims) {
        Object rawRoles = claims.get(CLAIM_ROLE_CODES);
        if (!(rawRoles instanceof List<?> values)) {
            return Set.of();
        }
        return RoleAuthorityUtils.normalizeRoleCodes(values.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toList()));
    }

    private String requireText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
