package xenosoft.imldintelligence.module.identity.internal.dto;

public record LoginResponse(
        AuthToken authToken,
        Long userId,
        String username,
        String tenantCode
) {
}