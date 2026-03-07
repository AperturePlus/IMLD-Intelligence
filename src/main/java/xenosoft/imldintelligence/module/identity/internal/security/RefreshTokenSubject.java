package xenosoft.imldintelligence.module.identity.internal.security;

public record RefreshTokenSubject(
        Long userId,
        Long tenantId
) {
}
