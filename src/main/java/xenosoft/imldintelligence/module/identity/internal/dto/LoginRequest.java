package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login 请求对象，封装Login相关的入参。
 */
public record LoginRequest(
        @NotBlank(message = "Username must not be blank")
        @Size(max = 100, message = "Username must be at most 100 characters")
        String username,
        @NotBlank(message = "Password must not be blank")
        @Size(max = 255, message = "Password must be at most 255 characters")
        String password,
        @Size(max = 64, message = "Tenant code must be at most 64 characters")
        String tenantCode
) {
}
