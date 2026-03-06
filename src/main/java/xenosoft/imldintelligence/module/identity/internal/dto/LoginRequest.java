package xenosoft.imldintelligence.module.identity.internal.dto;


public record LoginRequest(
        String username,
        String password,
        String tenantCode
) {
}
