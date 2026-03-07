package xenosoft.imldintelligence.module.identity.internal.security;

/**
 * 刷新令牌主体对象，封装刷新令牌中持有的最小身份信息。
 */
public record RefreshTokenSubject(
        Long userId,
        Long tenantId
) {
}
